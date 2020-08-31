# pliny-repo
This is the repository for the code for the Pliny software program (see http://pliny.cch.kcl.ac.uk)

Pliny is written in Java and is built using the Eclipse plugin framework.  Coupled with several Eclipse plugins that define most of Pliny's functionality is an Eclipse RCP program, called Pliny, which allows the plugins to be used as a standalone application.  The following Eclipse projects are stored as directories:

* uk.ac.kcl.cch.jb.pliny: provides the code for the basic Pliny functions including most of its views, and the Web Browser and Pliny note tools.
* uk.ac.kcl.cch.jb.pliny.feature: provides the files that allows the Pliny commponents to be packaged as an Eclipse feature.
* uk.ac.kcl.cch.jb.pliny.imageRes: provides the code for Pliny's image annotator
* uk.ac.kcl.cch.jb.pliny.pdfAnnot: provides the code for Pliny's PDF annotator
* uk.ac.kcl.cch.jb.pliny.rcp: provides the mechanism to allow Pliny to run standalone (using Eclipse's RCP framework)
* uk.ac.kcl.cch.jb.pliny.rcp.feature: makes the RCP software into an Eclipse feature
* uk.ac.kcl.cch.jb.pliny.update: provides the mechanism to package Pliny up as an Eclipse update site.

