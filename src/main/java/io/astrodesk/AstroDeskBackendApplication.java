package io.astrodesk;

import io.astrodesk.ticket.TicketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class AstroDeskBackendApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AstroDeskBackendApplication.class, args);
        TicketService service = context.getBean(TicketService.class);

        // Tymczasowe testy manualne
        /*
        Ticket t1 = service.createSampleTicket_1();
        Ticket t2 = service.createSampleTicket_2();
        Ticket t3 = service.createSampleTicket_3();

        service.showTickets().forEach(System.out::println);
        System.out.println(service.getTicket(2));

        service.acceptTicket(2);
        service.cancelTicket(3);
        service.startTicket(2);
        service.resolveTicket(2);
        service.closeTicket(2);
        service.showTickets().forEach(System.out::println);
        */
    }

}
