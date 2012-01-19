
package org.aavso.tools.vstar.input.ws.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.aavso.tools.vstar.input.ws.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetObservationsInRangeResponse_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getObservationsInRangeResponse");
    private final static QName _UnknownAUIDError_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "UnknownAUIDError");
    private final static QName _UnknownStarError_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "UnknownStarError");
    private final static QName _GetObservationsWithJDRangeResponse_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getObservationsWithJDRangeResponse");
    private final static QName _GetStarInfoByAUID_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getStarInfoByAUID");
    private final static QName _GetStarInfoByAUIDResponse_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getStarInfoByAUIDResponse");
    private final static QName _GetStarInfoByName_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getStarInfoByName");
    private final static QName _GetStarInfoByNameResponse_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getStarInfoByNameResponse");
    private final static QName _ConnectionException_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "ConnectionException");
    private final static QName _GetAllObservations_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getAllObservations");
    private final static QName _GetAllObservationsResponse_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getAllObservationsResponse");
    private final static QName _ObservationReadError_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "ObservationReadError");
    private final static QName _GetObservationsWithJDRange_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getObservationsWithJDRange");
    private final static QName _GetObservationsInRange_QNAME = new QName("http://endpoint.ws.input.vstar.tools.aavso.org/", "getObservationsInRange");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.aavso.tools.vstar.input.ws.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Magnitude }
     * 
     */
    public Magnitude createMagnitude() {
        return new Magnitude();
    }

    /**
     * Create an instance of {@link GetStarInfoByAUIDResponse }
     * 
     */
    public GetStarInfoByAUIDResponse createGetStarInfoByAUIDResponse() {
        return new GetStarInfoByAUIDResponse();
    }

    /**
     * Create an instance of {@link GetStarInfoByAUID }
     * 
     */
    public GetStarInfoByAUID createGetStarInfoByAUID() {
        return new GetStarInfoByAUID();
    }

    /**
     * Create an instance of {@link GetStarInfoByName }
     * 
     */
    public GetStarInfoByName createGetStarInfoByName() {
        return new GetStarInfoByName();
    }

    /**
     * Create an instance of {@link ValidObservation }
     * 
     */
    public ValidObservation createValidObservation() {
        return new ValidObservation();
    }

    /**
     * Create an instance of {@link UnknownStarError }
     * 
     */
    public UnknownStarError createUnknownStarError() {
        return new UnknownStarError();
    }

    /**
     * Create an instance of {@link ConnectionException }
     * 
     */
    public ConnectionException createConnectionException() {
        return new ConnectionException();
    }

    /**
     * Create an instance of {@link GetObservationsInRange }
     * 
     */
    public GetObservationsInRange createGetObservationsInRange() {
        return new GetObservationsInRange();
    }

    /**
     * Create an instance of {@link GetAllObservations }
     * 
     */
    public GetAllObservations createGetAllObservations() {
        return new GetAllObservations();
    }

    /**
     * Create an instance of {@link GetObservationsInRangeResponse }
     * 
     */
    public GetObservationsInRangeResponse createGetObservationsInRangeResponse() {
        return new GetObservationsInRangeResponse();
    }

    /**
     * Create an instance of {@link GetAllObservationsResponse }
     * 
     */
    public GetAllObservationsResponse createGetAllObservationsResponse() {
        return new GetAllObservationsResponse();
    }

    /**
     * Create an instance of {@link UnknownAUIDError }
     * 
     */
    public UnknownAUIDError createUnknownAUIDError() {
        return new UnknownAUIDError();
    }

    /**
     * Create an instance of {@link StarInfo }
     * 
     */
    public StarInfo createStarInfo() {
        return new StarInfo();
    }

    /**
     * Create an instance of {@link Ob }
     * 
     */
    public Ob createOb() {
        return new Ob();
    }

    /**
     * Create an instance of {@link ObservationReadError }
     * 
     */
    public ObservationReadError createObservationReadError() {
        return new ObservationReadError();
    }

    /**
     * Create an instance of {@link GetObservationsWithJDRange }
     * 
     */
    public GetObservationsWithJDRange createGetObservationsWithJDRange() {
        return new GetObservationsWithJDRange();
    }

    /**
     * Create an instance of {@link DateInfo }
     * 
     */
    public DateInfo createDateInfo() {
        return new DateInfo();
    }

    /**
     * Create an instance of {@link GetObservationsWithJDRangeResponse }
     * 
     */
    public GetObservationsWithJDRangeResponse createGetObservationsWithJDRangeResponse() {
        return new GetObservationsWithJDRangeResponse();
    }

    /**
     * Create an instance of {@link GetStarInfoByNameResponse }
     * 
     */
    public GetStarInfoByNameResponse createGetStarInfoByNameResponse() {
        return new GetStarInfoByNameResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetObservationsInRangeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getObservationsInRangeResponse")
    public JAXBElement<GetObservationsInRangeResponse> createGetObservationsInRangeResponse(GetObservationsInRangeResponse value) {
        return new JAXBElement<GetObservationsInRangeResponse>(_GetObservationsInRangeResponse_QNAME, GetObservationsInRangeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnknownAUIDError }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "UnknownAUIDError")
    public JAXBElement<UnknownAUIDError> createUnknownAUIDError(UnknownAUIDError value) {
        return new JAXBElement<UnknownAUIDError>(_UnknownAUIDError_QNAME, UnknownAUIDError.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnknownStarError }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "UnknownStarError")
    public JAXBElement<UnknownStarError> createUnknownStarError(UnknownStarError value) {
        return new JAXBElement<UnknownStarError>(_UnknownStarError_QNAME, UnknownStarError.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetObservationsWithJDRangeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getObservationsWithJDRangeResponse")
    public JAXBElement<GetObservationsWithJDRangeResponse> createGetObservationsWithJDRangeResponse(GetObservationsWithJDRangeResponse value) {
        return new JAXBElement<GetObservationsWithJDRangeResponse>(_GetObservationsWithJDRangeResponse_QNAME, GetObservationsWithJDRangeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStarInfoByAUID }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getStarInfoByAUID")
    public JAXBElement<GetStarInfoByAUID> createGetStarInfoByAUID(GetStarInfoByAUID value) {
        return new JAXBElement<GetStarInfoByAUID>(_GetStarInfoByAUID_QNAME, GetStarInfoByAUID.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStarInfoByAUIDResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getStarInfoByAUIDResponse")
    public JAXBElement<GetStarInfoByAUIDResponse> createGetStarInfoByAUIDResponse(GetStarInfoByAUIDResponse value) {
        return new JAXBElement<GetStarInfoByAUIDResponse>(_GetStarInfoByAUIDResponse_QNAME, GetStarInfoByAUIDResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStarInfoByName }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getStarInfoByName")
    public JAXBElement<GetStarInfoByName> createGetStarInfoByName(GetStarInfoByName value) {
        return new JAXBElement<GetStarInfoByName>(_GetStarInfoByName_QNAME, GetStarInfoByName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStarInfoByNameResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getStarInfoByNameResponse")
    public JAXBElement<GetStarInfoByNameResponse> createGetStarInfoByNameResponse(GetStarInfoByNameResponse value) {
        return new JAXBElement<GetStarInfoByNameResponse>(_GetStarInfoByNameResponse_QNAME, GetStarInfoByNameResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConnectionException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "ConnectionException")
    public JAXBElement<ConnectionException> createConnectionException(ConnectionException value) {
        return new JAXBElement<ConnectionException>(_ConnectionException_QNAME, ConnectionException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllObservations }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getAllObservations")
    public JAXBElement<GetAllObservations> createGetAllObservations(GetAllObservations value) {
        return new JAXBElement<GetAllObservations>(_GetAllObservations_QNAME, GetAllObservations.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllObservationsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getAllObservationsResponse")
    public JAXBElement<GetAllObservationsResponse> createGetAllObservationsResponse(GetAllObservationsResponse value) {
        return new JAXBElement<GetAllObservationsResponse>(_GetAllObservationsResponse_QNAME, GetAllObservationsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObservationReadError }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "ObservationReadError")
    public JAXBElement<ObservationReadError> createObservationReadError(ObservationReadError value) {
        return new JAXBElement<ObservationReadError>(_ObservationReadError_QNAME, ObservationReadError.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetObservationsWithJDRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getObservationsWithJDRange")
    public JAXBElement<GetObservationsWithJDRange> createGetObservationsWithJDRange(GetObservationsWithJDRange value) {
        return new JAXBElement<GetObservationsWithJDRange>(_GetObservationsWithJDRange_QNAME, GetObservationsWithJDRange.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetObservationsInRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", name = "getObservationsInRange")
    public JAXBElement<GetObservationsInRange> createGetObservationsInRange(GetObservationsInRange value) {
        return new JAXBElement<GetObservationsInRange>(_GetObservationsInRange_QNAME, GetObservationsInRange.class, null, value);
    }

}
