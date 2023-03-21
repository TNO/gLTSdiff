# gLTSdiff: Generalized LTS differencing

gLTSdiff is a library for comparing the structures of two or more generalized labeled transition systems (GLTSs), and merging them into a single combined GLTS, for example with colors that indicate how the structures of the input GLTSs relate.
This implementation is inspired by, and generalizes the LTSdiff algorithm proposed by Neil Walkinshaw and Kirill Bogdanov in their TOSEM'13 article titled "Automated Comparison of State-Based Software Models in Terms of Their Language and Structure" (see also [here](https://doi.org/10.1145/2430545.2430549)).
Moreover, gLTSdiff implementation comes with significant performance improvements over LTSdiff.

## Setting up a development environment

* Download the Eclipse Installer, from https://eclipse.org/downloads.
* Run the Eclipse Installer.
* Switch to Advanced mode, using the hamburger menu.
* Select `Eclipse Platform`, `2021-12` and `JRE 11.* - https://download.eclipse.org/justj/jres/11/updates/release/latest`.
* Click `Next`.
* Use the green plus button to add `https://raw.githubusercontent.com/TNO/gLTSdiff/main/com.github.tno.gltsdiff.setup`.
* Select `gLTSdiff` and press `Next`.
* Enable `Show all variables` and configure `Root install folder`, `Installation folder name`, `GitHub account full name` and `GitHub account email address`.
* Click `Next` and then click `Finish`.
* Once the installer is done, and a new development environment is launched, click `Finish` in the installer to close it.
