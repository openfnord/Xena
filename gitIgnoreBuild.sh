usage() {
  echo "usage: $0 ignore|view|revert"
  echo ""
  echo "  ignore:  Set git to ignore ReleaseInfo.java and buildnum.properties files"
  echo "           (based on setting assume unchanged)"
  echo "    view:  Show files currently being ignored"
  echo "  revert:  Cease ignoring files"
}

cd `dirname $0`
if [ $# -gt 0 ]; then
  if [ $1 = "ignore" ]; then
    echo "Setting git to ignore ReleaseInfo.java and buildnum.properties files"
    find . -name ReleaseInfo.java | git update-index --assume-unchanged --stdin
    find . -name buildnum.properties | git update-index --assume-unchanged --stdin
  elif [ $1 = "view" ]; then
    git ls-files -v | grep '^[[:lower:]]' | cut -c 3-
  elif [ $1 = "revert" ]; then
    echo "Reverting ignore of files by git using assume unchanged"
    git ls-files -v | grep '^[[:lower:]]' | cut -c 3- | git update-index --no-assume-unchanged --stdin
  else
    usage
  fi
else
  usage
fi
