package ru.nullpointer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.service.ClientException;
import ru.nullpointer.service.ExchangeRateService;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {TestBootstrap.class})
public class TestExchangeRateService {

    @Autowired
    private ExchangeRateService service;

    @Test
    public void testParseCBResponse() throws JAXBException, IOException, ParseException, ClientException {
        try (InputStream input = new FileInputStream("test/XML_daily.xml")) {
            Double result = service.processCBResponse(input, "USD");
            assertEquals(result, 61.1535, 0.0);
        }
    }

    @Test(expected = ClientException.class)
    public void testParseCBResponseWithUnknowCode() throws JAXBException, IOException, ParseException, ClientException {
        try (InputStream input = new FileInputStream("test/XML_daily.xml")) {
            service.processCBResponse(input, "XXX");
        }
    }

}
