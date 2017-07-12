import org.springframework.cloud.contract.spec.Contract
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

Contract.make {

    description "will return all reservations"

    request {
        method GET()
        url "/reservations"
    }
    response {
        body([[id: 1, reservationName: "Dan"], [id: 2, reservationName: "Jane"]])
        status 200
        headers {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
        }
    }
}