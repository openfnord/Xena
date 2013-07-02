/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 21/06/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.xml.transform.stream.StreamResult;

import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.kc7bfi.jflac.sound.spi.FlacFormatConversionProvider;
import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

public class AudioPlayerView extends XenaView {
	private static final int PLAYER_SAMPLE_SIZE_BITS = 16;
	private static final String PLAY_TEXT = "Play";
	private static final String PAUSE_TEXT = "Pause";
	private static final Logger logger = Logger.getLogger(AudioPlayerView.class.getName());

	private static final int STOPPED = 0;
	private static final int PLAYING = 1;
	private static final int PAUSED = 2;

	private volatile int playerStatus = STOPPED;

	private File flacFile;
	private SourceDataLine sourceLine;
	private JButton playPauseButton;
	private LineWriterThread lwThread;

	public AudioPlayerView() {
		super();
		initGUI();
	}
	
	private synchronized void setPlayerStatus(int playerStatus) {
		this.playerStatus = playerStatus;
	}

	private void initGUI() {
		final JPanel playerPanel = new JPanel(new FlowLayout());
		playPauseButton = new JButton(PLAY_TEXT);
		JButton stopButton = new JButton("Stop");
		playerPanel.add(playPauseButton);
		playerPanel.add(stopButton);
		this.add(playerPanel);

		playPauseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (playerStatus == PLAYING) {
					// Previously this action would use sourceLine.stop to cause the audio to stop.  For some reason this caused
					// the program to hang in some circumstances when using OpenJDK 7.  This seemed to be a locking issue.
					// The stopping functionality is now simply based on the lwThread.  This thread checks after each write for
					// a change of player status.  This allows for stopping to work in OpenJDK 7 but does introduce a small time
					// frame of playing after the button is pressed as the audio finishes playing what data is left in the buffer.
					setPlayerStatus(PAUSED);
					playPauseButton.setText(PLAY_TEXT);
				} else if (playerStatus == PAUSED) {
					// Previously sourceLine.start was used here.  Now we just notify the lwThread to continue writing to the buffer
					// to solve the same issues mentioned above
					setPlayerStatus(PLAYING);
					synchronized (lwThread) {
						lwThread.notify();
					}
					playPauseButton.setText(PAUSE_TEXT);
				} else if (playerStatus == STOPPED) {
					setPlayerStatus(PLAYING);
					playPauseButton.setText(PAUSE_TEXT);
					try {
						FlacAudioFileReader flacReader = new FlacAudioFileReader();
						AudioInputStream flacStream = flacReader.getAudioInputStream(flacFile);
						initAudioLine(flacStream);
					} catch (IOException ioe) {
						logger.log(Level.WARNING, "IOException when attempting to start playing audio file (" +
								   flacFile.getAbsolutePath() + "): " + ioe.getMessage(), ioe);
					} catch (UnsupportedAudioFileException uafe) {
						logger.log(Level.WARNING, "UnsupportedAudioFileException when attempting to start playing audio file (" +
								   flacFile.getAbsolutePath() + "): " + uafe.getMessage(), uafe);
					} catch (LineUnavailableException lue) {
						logger.log(Level.WARNING, "LineUnavailableException when attempting to start playing audio file (" +
								   flacFile.getAbsolutePath() + "): " + lue.getMessage(), lue);
					}
				}
				playerPanel.repaint();
			}

		});

		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPlayerStatus(STOPPED);
				playPauseButton.setText(PLAY_TEXT);
			}

		});

	}

	private void initAudioLine(AudioInputStream audioStream) throws LineUnavailableException {
		AudioFormat audioFormat = audioStream.getFormat();
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("Beggining initialisation of Audio Line for Audio Stream (Frame Size: " + audioFormat.getFrameSize() +
					      ", Frame Rate: " + audioFormat.getFrameRate() + ", Encoding: " + audioFormat.getEncoding().toString() + ")");
		}
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);

		if (!AudioSystem.isLineSupported(info)) {
			AudioFormat sourceFormat = audioFormat;
			AudioFormat targetFormat =
			    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), PLAYER_SAMPLE_SIZE_BITS, sourceFormat.getChannels(),
			                    sourceFormat.getChannels() * (PLAYER_SAMPLE_SIZE_BITS / 8), sourceFormat.getSampleRate(), sourceFormat.isBigEndian());

			// Use the flac converter to convert it to a raw stream.
			FlacFormatConversionProvider flacCoverter = new FlacFormatConversionProvider();
			audioStream = flacCoverter.getAudioInputStream(targetFormat, audioStream);
			audioFormat = audioStream.getFormat();
		}

		sourceLine = getSourceDataLine(audioFormat);
		sourceLine.start();

		lwThread = new LineWriterThread(audioStream, sourceLine.getBufferSize());
		lwThread.start();
	}

	/**
	 * Need to stop playback if the enclosing window or dialog is closed
	 */
	@Override
	protected void close() {
		setPlayerStatus(STOPPED);
		try {
			lwThread.join(1000); // wait one second maximum
		} catch (InterruptedException e) {
			// Just log and keep going
			logger.log(Level.WARNING, "InterruptedException while waiting for audio thread to stop: " +
					   e.getMessage(), e);
		}
		super.close();
	}

	// TODO: maybe can used by others. AudioLoop?
	// In this case, move to AudioCommon.
	private SourceDataLine getSourceDataLine(AudioFormat audioFormat) throws LineUnavailableException {
		/*
		 * Asking for a line is a rather tricky thing. We have to construct an Info object that specifies the desired
		 * properties for the line. First, we have to say which kind of line we want. The possibilities are:
		 * SourceDataLine (for playback), Clip (for repeated playback) and TargetDataLine (for recording). Here, we want
		 * to do normal playback, so we ask for a SourceDataLine. Then, we have to pass an AudioFormat object, so that
		 * the Line knows which format the data passed to it will have. Furthermore, we can give Java Sound a hint about
		 * how big the internal buffer for the line should be. Because of our issues with using the stop() function on the
		 * line we wish to set the internal buffer to be small enough so as not be very noticable that there can be a delay
		 * between the user pressing stop or pause and the actual cease of audio playing.  We try to get a buffer for about
		 * a quarter of a seconds audio for this purpose.
		 */
		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
		line = (SourceDataLine) AudioSystem.getLine(info);
		// limit the maximum buffer size so as to get around issues with the workaround we are currently using of simply letting
		// the buffer play until empty when stopping or pausing.  Using a smaller buffer in these circumstances makes the maximum
		// delay in stopping or pausing shorter (at the higher risk of buffer underrun).  If we can fix the hanging issue with
		// OpenJDK and put back in the use of sourceLine.stop() calls then this buffer size can be increased.
		int bufferSize = line.getBufferSize(); // default buffer size
		int frameSize = audioFormat.getFrameSize();
		float frameRate = audioFormat.getFrameRate();
		if (frameSize != AudioSystem.NOT_SPECIFIED && frameRate != AudioSystem.NOT_SPECIFIED) {
			int quarterSecondBuffer = frameSize * (int) (frameRate / 4);
			if (quarterSecondBuffer < bufferSize) {
				bufferSize = quarterSecondBuffer;
			}
		}
		if (bufferSize < info.getMinBufferSize()) {
			bufferSize = info.getMinBufferSize();
		}
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("For Audio playing using buffer size: " + bufferSize);
		}
		
		line.open(audioFormat, bufferSize);
		return line;
	}

	@Override
	public String getViewName() {
		return "audio";
	}

	@Override
	public boolean canShowTag(String tag) throws XenaException {
		String flacTag = DirectAudioNormaliser.AUDIO_PREFIX + ":" + DirectAudioNormaliser.FLAC_TAG;
		return tag.equals(flacTag);
	}

	@Override
	public ContentHandler getContentHandler() throws XenaException {
		FileOutputStream xenaTempOS = null;
		try {
			flacFile = File.createTempFile("tmpview", ".flac");
			flacFile.deleteOnExit();
			xenaTempOS = new FileOutputStream(flacFile);
		} catch (IOException e) {
			throw new XenaException("Problem creating temporary xena output file", e);
		}
		BinaryDeNormaliser base64Handler = new BinaryDeNormaliser();
		StreamResult result = new StreamResult(xenaTempOS);
		base64Handler.setResult(result);
		return base64Handler;
	}

	private class LineWriterThread extends Thread {
		private AudioInputStream audioStream;
		private int bufferSize;

		public LineWriterThread(AudioInputStream audioStream, int bufferSize) {
			this.audioStream = audioStream;
			this.bufferSize = bufferSize;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				int bytesRead;
				byte[] buffer = new byte[bufferSize];

				while (true) {
					if (playerStatus == PLAYING) {
						if (0 < (bytesRead = audioStream.read(buffer))) {
							sourceLine.write(buffer, 0, bytesRead);
						} else {
							// File has finished playing
							setPlayerStatus(STOPPED);
							playPauseButton.setText(PLAY_TEXT);
						}
					} else if (playerStatus == PAUSED) {
						try {
							synchronized (this) {
								wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else if (playerStatus == STOPPED) {
						audioStream.close();
						sourceLine.flush();
						sourceLine.close();
						break;
					}
				}
			} catch (IOException ioe) {
				logger.log(Level.WARNING, ioe.getMessage(), ioe);
			}
		}

	}
}
