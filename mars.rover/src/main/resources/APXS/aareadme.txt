PDS_VERSION_ID          = PDS3
RECORD_TYPE             = STREAM
OBJECT                  = TEXT
  PUBLICATION_DATE      = 2013-03-20
  NOTE                  = "N/A"
END_OBJECT              = TEXT

END

                              AAREADME.TXT
                            March 20, 2013

==============================================================================
INTRODUCTION
==============================================================================

This archive contains Reduced Data Record (RDR) data acquired by 
the Alpha Particle X-ray Spectrometer (APXS) instrument on the Mars Science 
Laboratory rover. 

==============================================================================
VOLUME SET INFORMATION
==============================================================================

APXS RDR data are stored in an archive volume having the directory 
structure illustrated below.  Raw APXS data products (EDRs) are stored in 
a separate archive volume not described here.

   root
    |
    |- AAREADME.TXT                Introduction to the archive (this file)
    |- ERRATA.TXT                  Release notes and errata for the archive
    |- VOLDESC.CAT                 Description of the contents of the archive
    |
    |- CALIB
    |     |
    |     |- CALINFO.TXT          Description of the CALIBRATION directory
    |     |- *.CSV, *.LBL         Calibration files used to develop 
    |                             calibration and reduction procedures,
    |                             and their PDS labels.
    |
    |- CATALOG
    |     |
    |     |- CATINFO.TXT           Description of the CATALOG directory
    |     |- APXS_INST.CAT         APXS instrument description
    |     |- APXS_PERSON.CAT       Relevant APXS personnel
    |     |- APXS_RDR_DS.CAT       APXS RDR data set description
    |     |- APXS_REF.CAT          References mentioned in APXS*.CAT files
    |     |- MSL_INSTHOST.CAT      MSL rover description 
    |     |- MSL_MISSION.CAT       MSL mission description
    |     |- MSL_REF.CAT           References mentioned in MSL*.CAT files
    |
    |- DATA
    |     |
    |     |- SOLnnnn               Directory of data acquired on sol nnnn
    |              |               
    |              |- *.CSV        APXS data products
    |              |- *.LBL        PDS labels describing data products
    |
    |- DOCUMENT
    |     |
    |     |- DOCINFO.TXT           Description of the DOCUMENT directory
    |     |- APXS_RDR_SIS.PDF,.HTM APXS RDR Software Interface Specification 
    |     |- APXS_RDR_SIS.LBL      PDS label for APXS_RDR_SIS.*
    |     |- PDSDD.FUL             PDS Data Dictionary
    |     |- PDSDD.LBL             PDS label for PDSDD.FUL
    |
    |
    |- INDEX
          |
          |- INDXINFO.TXT          Description of the INDEX directory
          |- INDEX.TAB             Table of data products in the archive
          |- INDEX.LBL             PDS label describing INDEX.TAB
     
    

==============================================================================
FILE FORMAT
==============================================================================

APXS data files are stored as ASCII tables with file names ending in
CSV. The tables are in comma-separated-value format, meaning that fields in
a row are separated by commas, and the fields are not padded with spaces to
be the same length in each row. Hence the lengths of rows in a table 
may vary. These files may be read in a text editor and are easily loaded
into spreadsheet programs.

Each table is accompanied by a detached PDS label with the same name but the 
extension LBL, which describes the format and content of the table.
PDS labels are ASCII text files that may be read in a text editor.

All text files in the archive, including PDS labels, are delimited with 
carriage-return (ASCII 13) line-feed (ASCII 10) pairs at the end of the line.

==============================================================================
COGNIZANT PERSONNEL
==============================================================================

APXS RDR data products are generated and assembled into archive volumes by 
the MSL APXS Science Team and delivered to Planetary Data System 
Geosciences Node. The Geosciences Node releases the archives to the public 
according to the MSL Project release schedule.

APXS Science Team Archive Representative: Ralf Gellert, rgellert@uoguelph.ca,
University of Guelph, Guelph, Ontario, Canada.

PDS Geosciences Node Representatives: Susan Slavney, Susan.Slavney@wustl.edu,
and Edward Guinness, guinness@wunder.wustl.edu, Washington University,
St. Louis, Missouri, USA.


