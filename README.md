# gLTSdiff: Generalized LTS differencing

gLTSdiff is a library for comparing the structures of two or more generalized labeled transition systems (GLTSs), and merging them into a single combined GLTS, for example with colors that indicate how the structures of the input GLTSs relate.
This implementation is inspired by, and generalizes the LTSdiff algorithm proposed by Neil Walkinshaw and Kirill Bogdanov in their TOSEM'13 article titled "Automated Comparison of State-Based Software Models in Terms of Their Language and Structure" (see also [here](https://doi.org/10.1145/2430545.2430549)).
Moreover, gLTSdiff implementation comes with significant performance improvements over LTSdiff.

## Setting up a development environment

* Download the Eclipse Installer, from https://eclipse.org/downloads.
* Run the Eclipse Installer.
* Switch to Advanced mode.
* Select _Eclipse Platform_, _2021-12_ and _JRE 11.* - https://download.eclipse.org/justj/jres/11/updates/release/latest_.
* Click _Next_.
* Use the green plus button to add `https://raw.githubusercontent.com/TNO/gLTSdiff/main/com.github.tno.gltsdiff.setup`.
* Select `gLTSdiff` and press _Next_.
* Enable _Show all variables_ and configure _Root install folder_, _Installation folder name_, _GitHub account full name_ and _GitHub account email address_.
* Click _Next_ and then click _Finish_.
* Once the installer is done, and a new development environment is launched, click _Finish_ in the installer to close it.
