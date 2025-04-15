package com.prova.cadastro;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.Set;

@Controller
public class UsuarioController {

    private final Set<String> emailsCadastrados = new HashSet<>();

    @PostMapping("/cadastro")
    public String processarCadastro(HttpServletRequest request) {
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        if (!StringUtils.hasText(nome) || !StringUtils.hasText(email) || !email.contains("@")) {
            return "redirect:/cadastro.html?erro=Preencha+corretamente";
        }

        synchronized (emailsCadastrados) {
            if (emailsCadastrados.contains(email.toLowerCase())) {
                return "redirect:/cadastro.html?erro=Email+já+cadastrado";
            }

            emailsCadastrados.add(email.toLowerCase());

            HttpSession session = request.getSession(true);
            session.setAttribute("nome", nome);

            return "redirect:/dashboard.html";
        }
    }

    @GetMapping("/sessao")
    @ResponseBody
    public String verificarSessao(HttpSession session) {
        String nome = (String) session.getAttribute("nome");
        return nome != null ? nome : "";
    }
}
