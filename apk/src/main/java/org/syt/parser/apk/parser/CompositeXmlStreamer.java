package org.syt.parser.apk.parser;

import org.syt.parser.apk.parser.XmlStreamer;
import org.syt.parser.apk.struct.xml.*;

/**
 * @author dongliu
 */
public class CompositeXmlStreamer implements org.syt.parser.apk.parser.XmlStreamer {

    public org.syt.parser.apk.parser.XmlStreamer[] xmlStreamers;

    public CompositeXmlStreamer(org.syt.parser.apk.parser.XmlStreamer... xmlStreamers) {
        this.xmlStreamers = xmlStreamers;
    }

    @Override
    public void onStartTag(XmlNodeStartTag xmlNodeStartTag) {
        for (org.syt.parser.apk.parser.XmlStreamer xmlStreamer : xmlStreamers) {
            xmlStreamer.onStartTag(xmlNodeStartTag);
        }
    }

    @Override
    public void onEndTag(XmlNodeEndTag xmlNodeEndTag) {
        for (org.syt.parser.apk.parser.XmlStreamer xmlStreamer : xmlStreamers) {
            xmlStreamer.onEndTag(xmlNodeEndTag);
        }
    }

    @Override
    public void onCData(XmlCData xmlCData) {
        for (org.syt.parser.apk.parser.XmlStreamer xmlStreamer : xmlStreamers) {
            xmlStreamer.onCData(xmlCData);
        }
    }

    @Override
    public void onNamespaceStart(XmlNamespaceStartTag tag) {
        for (org.syt.parser.apk.parser.XmlStreamer xmlStreamer : xmlStreamers) {
            xmlStreamer.onNamespaceStart(tag);
        }
    }

    @Override
    public void onNamespaceEnd(XmlNamespaceEndTag tag) {
        for (XmlStreamer xmlStreamer : xmlStreamers) {
            xmlStreamer.onNamespaceEnd(tag);
        }
    }
}
