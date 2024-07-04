package de.florian.banking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.javalin.Javalin;


public class Main {
    public static void main(String[] args) {

        final Logger LOGGER = LogManager.getLogger();

        var app = Javalin.create(/*config*/)
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);

        LOGGER.debug("Banking System started!");
    }
}