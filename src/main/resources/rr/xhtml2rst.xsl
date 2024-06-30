<?xml version="1.0" encoding="UTF-8"?>
<!--
  #%L
  JSQLParser library
  %%
  Copyright (C) 2004 - 2023 JSQLParser
  %%
  Dual licensed under GNU LGPL 2.1 or Apache License 2.0
  #L%
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:svg="http://www.w3.org/2000/svg"
                version="2.0"
                exclude-result-prefixes="#all"
>
    <xsl:output
            method="xml"
            encoding="utf8"
            omit-xml-declaration="yes"
            indent="no"/>

    <xsl:param name="withFloatingToc" select="'false'"/>
    <xsl:param name="isSnapshot" select="'false'"/>

    <!-- a default catch is needed to suppress all unwanted nodes -->
    <xsl:template match="*">
        <xsl:choose>
            <xsl:when test="$withFloatingToc='true'">
                <xsl:text disable-output-escaping="yes">
                    .. raw:: html

                    &lt;div id="floating-toc"&gt;
                    &lt;div class="search-container"&gt;
                    &lt;input type="button" id="toc-hide-show-btn"&gt;&lt;/input&gt;
                    &lt;input type="text" id="toc-search" placeholder="Search" /&gt;
                    &lt;/div&gt;
                    &lt;ul id="toc-list"&gt;&lt;/ul&gt;
                    &lt;/div&gt;

                </xsl:text>
            </xsl:when>
        </xsl:choose>

        <xsl:apply-templates select="/xhtml:html/xhtml:body"/>
    </xsl:template>

    <xsl:template match="/xhtml:html/xhtml:body">
        <xsl:text disable-output-escaping="yes">
            *********************************************************************
            SQL Syntax
        </xsl:text>
        <xsl:choose>
            <xsl:when test="$isSnapshot='true'">
                <xsl:text>|JSQLPARSER_SNAPSHOT_VERSION|</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>|JSQLPARSER_VERSION|</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>
            *********************************************************************

            The EBNF and Railroad Diagrams for
        </xsl:text>
        <xsl:choose>
            <xsl:when test="$isSnapshot='true'">
                <xsl:text>|JSQLPARSER_SNAPSHOT_VERSION|</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>|JSQLPARSER_VERSION|</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>.

        </xsl:text>
        <xsl:apply-templates select="svg:svg"/>
    </xsl:template>

    <xsl:template match="svg:svg[preceding-sibling::*[1]/xhtml:a]">
        <xsl:text disable-output-escaping="yes">
            ======================================================================================================================
        </xsl:text>
        <xsl:value-of select="translate(preceding-sibling::*[1]/xhtml:a/text(),'\:','')"/>
        <xsl:text disable-output-escaping="yes">
            ======================================================================================================================

        </xsl:text>

        <xsl:text disable-output-escaping="yes">
            .. raw:: html

        </xsl:text>

        <!-- SVG Diagram -->
        <xsl:copy-of select="."/>

        <table style="width:100%">
            <tbody>
                <tr>
                    <td style="width:67%" valign="top">
                        <!-- EBNF -->
                        <xsl:copy-of select="following-sibling::*[1]"/>
                    </td>
                    <td style="width:33%" valign="top">
                        <div class="ebnf">
                            <xsl:choose>
                                <!-- References -->
                                <xsl:when test="count(following-sibling::*[2]/xhtml:ul/xhtml:li)>0">
                                    Referenced by:
                                    <ul>
                                        <xsl:apply-templates
                                                select="following-sibling::*[2]/xhtml:ul/xhtml:li/xhtml:a"/>
                                    </ul>
                                </xsl:when>
                                <xsl:otherwise>
                                    Not referenced by any.
                                </xsl:otherwise>
                            </xsl:choose>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>

        <!-- empty Line -->
        <xsl:text disable-output-escaping="yes">

        </xsl:text>
    </xsl:template>

    <xsl:template match="xhtml:a">
        <li>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="@href"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:value-of select="@title"/>
                </xsl:attribute>
                <xsl:value-of select="text()"/>
            </a>
        </li>
    </xsl:template>
</xsl:stylesheet>
