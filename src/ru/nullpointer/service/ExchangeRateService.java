package ru.nullpointer.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.text.*;
import java.util.Date;

@Service
public class ExchangeRateService {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("dd/MM/yyyy"));
    private static ThreadLocal<NumberFormat> NUMBER_FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("#.####", symbols);
    });

    private CloseableHttpClient httpClient;
    private Unmarshaller unmarshaller;

    @PostConstruct
    public void init() throws JAXBException {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

        JAXBContext jaxbContext = JAXBContext.newInstance(CBResponse.class);
        unmarshaller = jaxbContext.createUnmarshaller();
    }

    @PreDestroy
    public void cleanUp() throws IOException {
        httpClient.close();
    }

    /**
     * Execute request to CB and return exchange rate for specified currency code and date
     * @param code currency code, in 3-letter format ('USD')
     * @param date requested date
     * @throws ClientException if no exchange rates available for specified date and code
     */
    public Double getRate(String code, Date date) throws IOException, JAXBException, ParseException, ClientException {
        HttpGet httpGet = new HttpGet("http://www.cbr.ru/scripts/XML_daily.asp?date_req="
                + DATE_FORMAT.get().format(date));
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            HttpEntity entity = httpResponse.getEntity();
            if (entity == null)  throw new IllegalStateException("No response from CB");
            return processCBResponse(entity.getContent(), code);
        }
    }

    public Double processCBResponse(InputStream inputStream, String code)
                                    throws JAXBException, ParseException, IOException, ClientException {
        CBResponse response = (CBResponse) unmarshaller.unmarshal(new StreamSource(inputStream));
        if (response.getValutes() == null)  throw new ClientException("No exchange rates for this date");
        for (CBResponse.Valute valute : response.getValutes()) {
            if (valute.getCharCode().equals(code)) {
                return NUMBER_FORMAT.get().parse(valute.getValue()).doubleValue();
            }
        }
        throw new ClientException("Unknown currency code: " + code);
    }

}
