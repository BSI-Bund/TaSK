<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
    <xsl:output encoding="UTF-8" indent="yes" method="xml"/>
    
    <!-- Set up variables-->
	<xsl:variable name="normalBGColor">#ffffff</xsl:variable>
	<xsl:variable name="accentBGColor">#d3d3d3</xsl:variable>

    <xsl:variable name="successColor">#009A17</xsl:variable>
    <xsl:variable name="warningColor">#ff9913</xsl:variable>
    <xsl:variable name="failureColor">#e10600</xsl:variable>
	

    
    <xsl:template match="TaSKReport">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4-portrail" page-height="297mm" page-width="210mm" margin-top="5mm" margin-bottom="5mm" margin-left="5mm" margin-right="5mm">
                    <fo:region-body region-name="xsl-region-body" margin-top="10mm" margin-bottom="20mm"/>
                    <fo:region-before region-name="xsl-region-before" extent="10mm" display-align="before" precedence="true"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="A4-portrail">
                <fo:static-content flow-name="xsl-region-before">
                	<fo:block text-align="center" font-size="18pt">TaSK Report</fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body" border-collapse="collapse" reference-orientation="0">
            
            		<!-- Metadata -->
                    <xsl:apply-templates select="Metadata">
                    	<xsl:with-param name="chapter">1</xsl:with-param>
                    </xsl:apply-templates>

                    <!-- DUT Information -->
                    <xsl:apply-templates select="DUTInformation">
                        <xsl:with-param name="chapter">2</xsl:with-param>
                    </xsl:apply-templates>

                    <!-- Conformity Statements -->
                    <xsl:apply-templates select="." mode="Conformity">
                        <xsl:with-param name="chapter">3</xsl:with-param>
                    </xsl:apply-templates>

                    <!-- Summaries -->
                    <xsl:apply-templates select="." mode="TSSummary">
                    	<xsl:with-param name="chapter">4</xsl:with-param>
                    </xsl:apply-templates>
	                 
	                <!-- Test Collection Details -->
	                <xsl:apply-templates select="." mode="TSDetails">
	                	<xsl:with-param name="chapter">5</xsl:with-param>
                    </xsl:apply-templates>
	                
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
    
    <xsl:template match="Metadata">
    	<xsl:param name="chapter" />
    	<!-- Metadata -->
	   	<fo:block font-size="16pt" margin-bottom="2mm"><xsl:number format="1. " value="$chapter" level="multiple"/>General Information</fo:block>
	   	<fo:table table-layout="fixed" width="100%" font-size="10pt" margin-bottom="2mm">
	           <fo:table-column column-width="proportional-column-width(20)"/>
	           <fo:table-column column-width="proportional-column-width(80)"/>
	           <fo:table-body>
	               <fo:table-row>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           Report Generated:
	                       </fo:block>
	                   </fo:table-cell>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           <xsl:value-of select="DateOfReportGeneration"/>
	                       </fo:block>
	                   </fo:table-cell>
	               </fo:table-row>
	               <fo:table-row>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           Start of Execution:
	                       </fo:block>
	                   </fo:table-cell>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           <xsl:value-of select="StartOfExecution"/>
	                       </fo:block>
	                   </fo:table-cell>
	               </fo:table-row>
	               <fo:table-row>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           End of Execution:
	                       </fo:block>
	                   </fo:table-cell>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           <xsl:value-of select="EndOfExecution"/>
	                       </fo:block>
	                   </fo:table-cell>
	               </fo:table-row>
	               <fo:table-row>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           Tester in Charge:
	                       </fo:block>
	                   </fo:table-cell>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           <xsl:value-of select="TesterInCharge"/>
	                       </fo:block>
	                   </fo:table-cell>
	               </fo:table-row>
	               <fo:table-row>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           Execution Machine:
	                       </fo:block>
	                   </fo:table-cell>
	                   <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
	                       <fo:block>
	                           <xsl:value-of select="ExecutionMachine"/>
	                       </fo:block>
	                   </fo:table-cell>
	               </fo:table-row>
                   <fo:table-row>
                       <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                           <fo:block>
                               Execution Type:
                           </fo:block>
                       </fo:table-cell>
                       <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                           <fo:block>
                               <xsl:value-of select="ExecutionType"/>
                           </fo:block>
                       </fo:table-cell>
                   </fo:table-row>
	           </fo:table-body>
	       </fo:table>
    </xsl:template>

    <xsl:template match="DUTInformation">
        <xsl:param name="chapter" />
        <!-- Metadata -->
        <fo:block font-size="16pt" margin-bottom="2mm"><xsl:number format="1. " value="$chapter" level="multiple"/>Information about the Device Under Test</fo:block>
        <fo:table table-layout="fixed" width="100%" font-size="10pt" margin-bottom="2mm">
            <fo:table-column column-width="proportional-column-width(20)"/>
            <fo:table-column column-width="proportional-column-width(80)"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            Title:
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            <xsl:value-of select="Title"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <xsl:if test="ApplicationType">
                    <fo:table-row>
                        <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                            <fo:block>
                                Application Type:
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                            <fo:block>
                                <xsl:value-of select="ApplicationType"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="Version">
                    <fo:table-row>
                        <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                            <fo:block>
                                Version:
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                            <fo:block>
                                <xsl:value-of select="Version"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="File">
                    <fo:table-row>
                        <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                            <fo:block>
                                Input Filepath:
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                            <fo:block>
                                <xsl:value-of select="File"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="Fingerprint">
                    <fo:table-row>
                        <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                            <fo:block>
                                SHA-256 Fingerprint of Inputfile:
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                            <fo:block>
                                <xsl:value-of select="Fingerprint"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="TaSKReport" mode="Conformity">
        <!-- Params -->
        <xsl:param name="chapter" />

        <!-- Executed Test Collections -->
        <fo:block font-size="16pt" margin-bottom="2mm"><xsl:number format="1. " value="$chapter" level="multiple"/>Conformity</fo:block>
        <fo:block margin-left="4mm" font-size="12pt" margin-bottom="2mm">
            <xsl:if test="TestSuite">
                <xsl:for-each select="TestSuite">
                    <xsl:if test="TestSuiteIdentifier = 'ICS Checklist'">
                        <fo:block>
                            The MICS of the Device Under Test is
                            <xsl:choose>
                                <xsl:when test="Summary/NoOfFailedTestcases > 0"><fo:inline color="#e10600">not conform</fo:inline></xsl:when>
                                <xsl:otherwise><fo:inline color="#009A17">conform</fo:inline></xsl:otherwise>
                            </xsl:choose>
                            with TR-03116-TS.
                        </fo:block>
                    </xsl:if>
                    <xsl:if test="TestSuiteIdentifier = 'Certificate Checks'">
                        <fo:block> The Certificates of the Device Under Test are
                            <xsl:choose>
                                <xsl:when test="Summary/NoOfFailedTestcases > 0"><fo:inline color="#e10600">not conform</fo:inline></xsl:when>
                                <xsl:otherwise><fo:inline color="#009A17">conform</fo:inline></xsl:otherwise>
                            </xsl:choose>
                            with TR-03116-TS.</fo:block>
                    </xsl:if>
                    <xsl:if test="TestSuiteIdentifier = 'TaSK TLS TestSuite'">
                        <fo:block> The test cases of TR-03116-TS have been executed
                            <xsl:choose>
                                <xsl:when test="Summary/NoOfFailedTestcases > 0"><fo:inline color="#e10600">with errors</fo:inline>.</xsl:when>
                                <xsl:otherwise><fo:inline color="#009A17">successfully</fo:inline>.</xsl:otherwise>
                            </xsl:choose>
                        </fo:block>
                    </xsl:if>
                    <xsl:if test="((ApplicationType != '') and (TestSuiteIdentifier = 'TaSK TLS TestSuite'))">
                        <fo:block> The tested <xsl:value-of select="ApplicationType" /> is
                            <xsl:choose>
                                <xsl:when test="Summary/NoOfFailedTestcases > 0"><fo:inline color="#e10600">not conform</fo:inline>.</xsl:when>
                                <xsl:otherwise><fo:inline color="#009A17">conform</fo:inline>.</xsl:otherwise>
                            </xsl:choose>
                            with the requirements from TR-03116-TS Annex.</fo:block>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
        </fo:block>
    </xsl:template>

    <xsl:template match="TaSKReport" mode="TSSummary">
    	<!-- Params -->
    	<xsl:param name="chapter" />
    
    	<!-- Executed Test Collections -->
        <fo:block font-size="16pt" margin-bottom="2mm"><xsl:number format="1. " value="$chapter" level="multiple"/>Summary</fo:block>
        
        <fo:block margin-left="4mm" font-size="14pt" margin-bottom="2mm"><xsl:value-of select="$chapter"/>.1 Executed Test Suite Modules</fo:block>
        
        <fo:table table-layout="fixed" width="100%" font-size="10pt" border-color="black" text-align="center" display-align="center" space-after="5mm">
            <fo:table-column column-width="proportional-column-width(20)"/>
            <fo:table-column column-width="proportional-column-width(10)"/>
            <fo:table-column column-width="proportional-column-width(10)"/>
            <fo:table-column column-width="proportional-column-width(10)"/>
            <fo:table-column column-width="proportional-column-width(10)"/>
            <fo:table-column column-width="proportional-column-width(10)"/>
            <fo:table-column column-width="proportional-column-width(15)"/>
            <fo:table-column column-width="proportional-column-width(15)"/>
            <fo:table-header font-weight="600">
            	<fo:table-row height="8mm">
            		<xsl:attribute name="background-color">
					  	<xsl:value-of select="$accentBGColor"/>
					</xsl:attribute>
                    <fo:table-cell>
                        <fo:block>Identifier</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block>Total</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block>Executed</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block>Passed</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block>With warnings</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block>Failed</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block>Start time</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block>End time</fo:block>
                    </fo:table-cell>
                </fo:table-row>
           	</fo:table-header>
            <fo:table-body font-size="95%">
                <xsl:choose>
                    <xsl:when test="TestSuite">
                        <xsl:for-each select="TestSuite">
                            <fo:table-row>
                                <xsl:attribute name="background-color">
                                    <xsl:choose>
                                        <xsl:when test="(position() mod 2) = 0"><xsl:value-of select="$accentBGColor"/></xsl:when>
                                        <xsl:otherwise><xsl:value-of select="$normalBGColor"/></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <fo:table-cell>
                                    <xsl:attribute name="color">
                                        <xsl:choose>
                                            <xsl:when test="Summary/NoOfFailedTestcases > 0"><xsl:value-of select="$failureColor"/></xsl:when>
                                            <xsl:otherwise><xsl:value-of select="$successColor"/></xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:attribute>
                                    <fo:block>
                                    	<fo:basic-link internal-destination="{TestSuiteIdentifier}">
	                                        <xsl:value-of select="TestSuiteIdentifier"/>
                                    	</fo:basic-link>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="Summary/TotalNoOfTestcases"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="Summary/NoOfExecTestcases"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="Summary/NoOfPassedTestcases"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="Summary/NoOfTestcasesWithWarnings"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="Summary/NoOfFailedTestcases"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:variable name="startTime" select="Summary/StartTime"/>
                                        <xsl:value-of select="translate($startTime, 'T', '&#xd;')"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:variable name="endTime" select="Summary/EndTime"/>
                                        <xsl:value-of select="translate($endTime, 'T', '&#xd;')"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <fo:table-row>
                            <xsl:attribute name="background-color">
                                <xsl:choose>
                                    <xsl:when test="(position() mod 2) = 0"><xsl:value-of select="$accentBGColor"/></xsl:when>
                                    <xsl:otherwise><xsl:value-of select="$normalBGColor"/></xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <fo:table-cell>
                                <fo:block>
                                    None
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    None
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    None
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    None
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    None
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    None
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    None
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    None
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </xsl:otherwise>
                </xsl:choose>

            </fo:table-body>
        </fo:table>
            
        <!-- Test Collection Summaries -->
        <fo:block margin-left="4mm" font-size="14pt" margin-bottom="2mm"><xsl:value-of select="$chapter"/>.2 Summaries of Test Suite Modules</fo:block>
        <xsl:for-each select="TestSuite">
         <fo:block margin-left="6mm" id="{TestSuiteIdentifier}"><xsl:value-of select="$chapter"/>.2.<xsl:number format="1 " count="TestSuite"/> Summary of Module "<xsl:value-of select="TestSuiteIdentifier"/>"</fo:block>
         <fo:table table-layout="fixed" width="100%" font-size="10pt" text-align="center" display-align="center" space-after="5mm">
             <fo:table-column column-width="proportional-column-width(20)"/>
             <fo:table-column column-width="proportional-column-width(20)"/>
             <fo:table-column column-width="proportional-column-width(40)"/>
             <fo:table-header font-weight="600">
             	<fo:table-row height="8mm">
             		<xsl:attribute name="background-color">
					   	<xsl:value-of select="$accentBGColor"/>
					</xsl:attribute>
                     <fo:table-cell>
                         <fo:block>Identifier</fo:block>
                     </fo:table-cell>
                     <fo:table-cell>
                         <fo:block>Result</fo:block>
                     </fo:table-cell>
                 </fo:table-row>
             </fo:table-header>
             <fo:table-body font-size="95%">
                 <xsl:choose>
                     <xsl:when test="TestCases/TestCase">
                         <xsl:for-each select="TestCases/TestCase">
                             <fo:table-row>
                                 <xsl:attribute name="background-color">
                                     <xsl:choose>
                                         <xsl:when test="(position() mod 2) = 0"><xsl:value-of select="$accentBGColor"/></xsl:when>
                                         <xsl:otherwise><xsl:value-of select="$normalBGColor"/></xsl:otherwise>
                                     </xsl:choose>
                                 </xsl:attribute>

                                 <fo:table-cell>
                                     <fo:block>
						                <fo:basic-link internal-destination="{TestCaseId}">
						                	<xsl:value-of select="TestCaseId"></xsl:value-of>
						                </fo:basic-link>
                                     </fo:block>
                                 </fo:table-cell>
                                 <fo:table-cell>
                                     <fo:block>
		                                 <xsl:attribute name="color">
		                                     <xsl:choose>
		                                         <xsl:when test="string(Result) = 'PASSED'"><xsl:value-of select="$successColor"/></xsl:when>
		                                         <xsl:when test="string(Result)  = 'PASSED_WITH_WARNINGS'"><xsl:value-of select="$warningColor"/></xsl:when>
		                                         <xsl:otherwise><xsl:value-of select="$failureColor"/></xsl:otherwise>
		                                     </xsl:choose>
		                                 </xsl:attribute>
                                         <xsl:value-of select="Result"/>
                                     </fo:block>
                                 </fo:table-cell>
                             </fo:table-row>
                         </xsl:for-each>
                     </xsl:when>
                     <xsl:otherwise>
                         <fo:table-row>
                             <xsl:attribute name="background-color">
                                 <xsl:choose>
                                     <xsl:when test="(position() mod 2) = 0"><xsl:value-of select="$accentBGColor"/></xsl:when>
                                     <xsl:otherwise><xsl:value-of select="$normalBGColor"/></xsl:otherwise>
                                 </xsl:choose>
                             </xsl:attribute>
                             <xsl:attribute name="color">
                                 <xsl:value-of select="$failureColor"/>
                             </xsl:attribute>

                             <fo:table-cell>
                                 <fo:block>
                                     None
                                 </fo:block>
                             </fo:table-cell>
                             <fo:table-cell>
                                 <fo:block>
                                     None
                                 </fo:block>
                             </fo:table-cell>
                         </fo:table-row>
                     </xsl:otherwise>
                 </xsl:choose>
             </fo:table-body>
         </fo:table>
     </xsl:for-each>
    
    </xsl:template>
    
    <xsl:template match="TaSKReport" mode="TSDetails">
    	<!-- Params -->
    	<xsl:param name="chapter" />
    
    	<fo:block font-size="16pt" margin-bottom="2mm" page-break-before="always"><xsl:number format="1. " value="$chapter" level="multiple"/>Test Case Collection Details</fo:block>
        <xsl:for-each select="TestSuite">
         <fo:block font-size="14pt" margin-left="4mm" margin-bottom="2mm"><xsl:value-of select="$chapter"/>.<xsl:number format="1 " count="TestSuite"/> Details of Collection "<xsl:value-of select="TestSuiteIdentifier"/>"</fo:block>
         <xsl:for-each select="TestCases/TestCase">
         	<fo:block font-size="12pt" text-align="left" margin-left="6mm" margin-bottom="2mm">
                <xsl:attribute name="id">
                	<!-- add an ID as link anchor -->
                	<xsl:value-of select="TestCaseId"></xsl:value-of>
                </xsl:attribute>
                <xsl:value-of select="TestCaseId"></xsl:value-of>
            </fo:block>
          <fo:table table-layout="fixed" width="100%" font-size="10pt" margin-bottom="4mm">
            <fo:table-column column-width="proportional-column-width(20)"/>
            <fo:table-column column-width="proportional-column-width(80)"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            Purpose:
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            <xsl:value-of select="Purpose"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            Result:
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            <xsl:attribute name="color">
                                <xsl:choose>
                                    <xsl:when test="string(Result) = 'PASSED'"><xsl:value-of select="$successColor"/></xsl:when>
                                    <xsl:when test="string(Result)  = 'PASSED_WITH_WARNINGS'"><xsl:value-of select="$warningColor"/></xsl:when>
                                    <xsl:otherwise><xsl:value-of select="$failureColor"/></xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:value-of select="Result"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            Start time:
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            <xsl:value-of select="StartTime"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            End time:
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            <xsl:value-of select="EndTime"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <xsl:if test="not(Result='PASSED')">
                <fo:table-row>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block>
                            Additional Information:
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="left" display-align="center" padding-left="2mm">
                        <fo:block></fo:block>
                         <xsl:for-each select="LogMessages/LogMessage[not(@LogLevel='INFO' or @LogLevel='STEP')]">
                             <fo:block>
                                 <xsl:value-of select="."/>
                             </fo:block>                                    
                         </xsl:for-each>
                    </fo:table-cell>
                </fo:table-row>
                </xsl:if>
            </fo:table-body>
        	</fo:table>
         </xsl:for-each>
     </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>