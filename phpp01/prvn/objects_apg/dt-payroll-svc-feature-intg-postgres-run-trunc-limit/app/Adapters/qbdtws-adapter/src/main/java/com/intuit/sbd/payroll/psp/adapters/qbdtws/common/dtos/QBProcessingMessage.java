package com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 8, 2009
 * Time: 8:52:33 AM
 */
@XmlRootElement
@XmlType(name = "QBProcessingMessage")
public class QBProcessingMessage {
    private int code;
    private QBProcessingMessageLevel level;
    private String message;

    // non-xml serialized fields
    private String entitySourceId;

    @XmlElement(name = "Code", required = true)
    public int getCode() {
        return code;
    }

    public void setCode(int pCode) {
        code = pCode;
    }

    @XmlElement(name = "Level", required = true)
    public QBProcessingMessageLevel getLevel() {
        return level;
    }

    public void setLevel(QBProcessingMessageLevel level) {
        this.level = level;
    }

    @XmlElement(name = "Message", required = true)
    public String getMessage() {
        return message;
    }

    public void setMessage(String pMessage) {
        message = pMessage;
    }

    public String getEntitySourceId() {
        return entitySourceId;
    }

    public void setEntitySourceId(String entitySourceId) {
        this.entitySourceId = entitySourceId;
    }

    @Override
    public String toString() {
        return "QBProcessingMessage{" +
                "code=" + code +
                ", level=" + level +
                ", message='" + message + '\'' +
                '}';
    }
}
