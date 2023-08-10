# GraphViz dependency

gLTSdiff uses [GraphViz](https://graphviz.org/) to render DOT files to SVG images.
GraphViz can be downloaded at https://graphviz.org/download, but is also available in various package managers.

Make sure that GraphViz is installed or extracted on your system.
And that its `bin` directory is in your `PATH` environment variable.
Alternatively, you can set the `DOT_PATH` environment variable to the path of the `dot` executable with that `bin` directory.

For information on how to set environment variables can be found on the internet.
For instance, see:

* https://www.java.com/en/download/help/path.html
* https://www.educative.io/answers/how-to-add-an-application-path-to-system-environment-variables

Note that on Windows, you can use _Edit environment variables for your account_ rather than _Edit the system environment variables_, if you do not have the required permissions to edit the system environment variables.
