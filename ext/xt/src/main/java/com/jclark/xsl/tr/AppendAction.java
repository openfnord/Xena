// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 *
 */
class AppendAction implements Action
{
    private Vector sequence = new Vector();

    public void invoke(ProcessContext context, 
                       Node sourceNode, Result result) throws XSLException
    {
        for (Enumeration enumx = sequence.elements();
             enumx.hasMoreElements();)
            ((Action)enumx.nextElement()).invoke(context, sourceNode, result);
    }

    void add(Action action)
    {
        sequence.addElement(action);
    }
}
  
