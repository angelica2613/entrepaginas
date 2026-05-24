package com.example.Entrepaginas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping({"/maraton", "/maraton.html"})
    public String maraton() {
        return "maraton";
    }


    @GetMapping({"/eventos", "/eventos.html"})
    public String eventos() {
        return "eventos";
    }

    @GetMapping({"/escribe", "/escribe.html"})
    public String escribe() {
        return "escribe";
    }

    @GetMapping({"/contacto", "/contacto.html"})
    public String contacto() {
        return "contacto";
    }

}
