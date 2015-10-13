package ru.nullpointer.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.nullpointer.service.ClientException;
import ru.nullpointer.service.ExchangeRateService;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.text.*;
import java.util.Date;

@RestController
@ControllerAdvice
public class ExchangeRateEndPoint {

    private static Log log = LogFactory.getLog(ExchangeRateEndPoint.class);

    private static final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        return dateFormat;
    });
    private static final ThreadLocal<NumberFormat> NUMBER_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#.####"));

    @Autowired
    private ExchangeRateService service;

    @RequestMapping("/api/rate/{code}")
    @ResponseBody
    public ExchangeRateResponse rate(@PathVariable String code)
                                     throws IOException, ClientException, ParseException, JAXBException {
        return rateByDate(code, DATE_FORMAT.get().format(new Date()));  //pass current date
    }

    @RequestMapping("/api/rate/{code}/{date}")
    @ResponseBody
    public ExchangeRateResponse rateByDate(@PathVariable String code, @PathVariable String date)
                                           throws IOException, JAXBException, ParseException, ClientException {
        //    parse parameters
        if (!code.matches("[A-Z]{3}"))  throw new ClientException("Wrong currency code: " + code);
        Date dateValue;
        try {
            dateValue = DATE_FORMAT.get().parse(date);
        } catch (ParseException e) {
            throw new ClientException("Wrong date format: " + date);
        }

        //    execute
        Double rate = service.getRate(code, dateValue);

        //    return response
        return new ExchangeRateResponse(code, NUMBER_FORMAT.get().format(rate), date);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ClientException.class)
    public ErrorResponse handleConflict(ClientException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleConflict(Exception e) {
        log.error("", e);
        return new ErrorResponse("Internal server error");
    }
}