<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text" encoding="UTF-8"/>

  <xsl:variable name="pad" select="'                                                  '"/>
  <xsl:variable name="dashes" select="'--------------------------------------------------'"/>

  <xsl:template name="right-pad">
    <xsl:param name="str"/>
    <xsl:param name="width"/>
    <xsl:value-of select="substring(concat($str, $pad), 1, $width)"/>
  </xsl:template>

  <xsl:template name="left-pad">
    <xsl:param name="str"/>
    <xsl:param name="width"/>
    <xsl:value-of select="substring(concat($pad, $str), string-length(concat($pad, $str)) - $width + 1)"/>
  </xsl:template>

  <xsl:template name="pct">
    <xsl:param name="missed"/>
    <xsl:param name="covered"/>
    <xsl:variable name="total" select="$missed + $covered"/>
    <xsl:choose>
      <xsl:when test="$total &gt; 0">
        <xsl:value-of select="format-number($covered div $total * 100, '0.0')"/>
        <xsl:text>%</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>  n/a</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="header-row">
    <xsl:call-template name="right-pad"><xsl:with-param name="str" select="'Package'"/><xsl:with-param name="width" select="50"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'Lines'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'Line%'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'Branch'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'Brch%'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'Method'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'Mthd%'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="separator-row">
    <xsl:call-template name="right-pad"><xsl:with-param name="str" select="$dashes"/><xsl:with-param name="width" select="50"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'------'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'------'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'------'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'------'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'------'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="'------'"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="data-row">
    <xsl:param name="label"/>
    <xsl:param name="line-m"/><xsl:param name="line-c"/>
    <xsl:param name="branch-m"/><xsl:param name="branch-c"/>
    <xsl:param name="method-m"/><xsl:param name="method-c"/>

    <xsl:call-template name="right-pad"><xsl:with-param name="str" select="$label"/><xsl:with-param name="width" select="50"/></xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="$line-m + $line-c"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad">
      <xsl:with-param name="str"><xsl:call-template name="pct"><xsl:with-param name="missed" select="$line-m"/><xsl:with-param name="covered" select="$line-c"/></xsl:call-template></xsl:with-param>
      <xsl:with-param name="width" select="7"/>
    </xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="$branch-m + $branch-c"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad">
      <xsl:with-param name="str"><xsl:call-template name="pct"><xsl:with-param name="missed" select="$branch-m"/><xsl:with-param name="covered" select="$branch-c"/></xsl:call-template></xsl:with-param>
      <xsl:with-param name="width" select="7"/>
    </xsl:call-template>
    <xsl:call-template name="left-pad"><xsl:with-param name="str" select="$method-m + $method-c"/><xsl:with-param name="width" select="7"/></xsl:call-template>
    <xsl:call-template name="left-pad">
      <xsl:with-param name="str"><xsl:call-template name="pct"><xsl:with-param name="missed" select="$method-m"/><xsl:with-param name="covered" select="$method-c"/></xsl:call-template></xsl:with-param>
      <xsl:with-param name="width" select="7"/>
    </xsl:call-template>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template match="/report">
    <xsl:text>&#10;VStar Coverage Summary&#10;</xsl:text>
    <xsl:text>======================&#10;&#10;</xsl:text>

    <xsl:call-template name="header-row"/>
    <xsl:call-template name="separator-row"/>

    <xsl:for-each select="package">
      <xsl:variable name="short" select="translate(@name, '/', '.')"/>
      <xsl:variable name="label">
        <xsl:choose>
          <xsl:when test="starts-with($short, 'org.aavso.tools.vstar.')">
            <xsl:value-of select="concat('..', substring-after($short, 'org.aavso.tools.vstar'))"/>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="$short"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:call-template name="data-row">
        <xsl:with-param name="label" select="$label"/>
        <xsl:with-param name="line-m" select="counter[@type='LINE']/@missed"/>
        <xsl:with-param name="line-c" select="counter[@type='LINE']/@covered"/>
        <xsl:with-param name="branch-m" select="counter[@type='BRANCH']/@missed"/>
        <xsl:with-param name="branch-c" select="counter[@type='BRANCH']/@covered"/>
        <xsl:with-param name="method-m" select="counter[@type='METHOD']/@missed"/>
        <xsl:with-param name="method-c" select="counter[@type='METHOD']/@covered"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:call-template name="separator-row"/>

    <xsl:call-template name="data-row">
      <xsl:with-param name="label" select="'TOTAL'"/>
      <xsl:with-param name="line-m" select="counter[@type='LINE']/@missed"/>
      <xsl:with-param name="line-c" select="counter[@type='LINE']/@covered"/>
      <xsl:with-param name="branch-m" select="counter[@type='BRANCH']/@missed"/>
      <xsl:with-param name="branch-c" select="counter[@type='BRANCH']/@covered"/>
      <xsl:with-param name="method-m" select="counter[@type='METHOD']/@missed"/>
      <xsl:with-param name="method-c" select="counter[@type='METHOD']/@covered"/>
    </xsl:call-template>

    <xsl:text>&#10;</xsl:text>
  </xsl:template>

</xsl:stylesheet>
