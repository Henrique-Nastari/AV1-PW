package ufrn.br.lojapw.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ufrn.br.lojapw.model.Cliente;
import ufrn.br.lojapw.model.Lojista;
import ufrn.br.lojapw.repository.ClienteRepository;
import ufrn.br.lojapw.repository.LojistaRepository;

@Controller
public class LoginController {

    private final ClienteRepository clienteRepository;
    private final LojistaRepository lojistaRepository;

    public LoginController(ClienteRepository clienteRepository, LojistaRepository lojistaRepository) {
        this.clienteRepository = clienteRepository;
        this.lojistaRepository = lojistaRepository;
    }

    // Página de Login
    @GetMapping("/login")
    public String mostrarLogin() {
        return "/login.html"; // Vai abrir o arquivo login.html dentro da pasta /static
    }

    // Processar Login
    @PostMapping("/fazer-login")
    public String fazerLogin(HttpServletRequest request) {
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        Cliente cliente = clienteRepository.findByEmail(email);
        if (cliente != null && cliente.getSenha().equals(senha)) {
            // É um cliente
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogado", cliente);
            session.setAttribute("tipoUsuario", "cliente");
            session.setMaxInactiveInterval(20 * 60); // 20 minutos
            return "redirect:/produtos";
        }

        Lojista lojista = lojistaRepository.findByEmail(email);
        if (lojista != null && lojista.getSenha().equals(senha)) {
            // É um lojista
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogado", lojista);
            session.setAttribute("tipoUsuario", "lojista");
            session.setMaxInactiveInterval(20 * 60); // 20 minutos
            return "redirect:/produtos";
        }

        // Se não encontrar usuário válido
        return "redirect:/login?erro=true";
    }

    // Página de Cadastro de Cliente
    @GetMapping("/cadastro")
    public String mostrarCadastro() {
        return "/cadastroCliente.html"; // Arquivo HTML simples
    }

    // Processar Cadastro de Cliente
    @PostMapping("/cadastrar-cliente")
    public String cadastrarCliente(HttpServletRequest request) {
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        // Salvar novo cliente
        Cliente cliente = new Cliente(nome, email, senha);
        clienteRepository.save(cliente);

        return "redirect:/login";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Destroi a sessão
        return "redirect:/login";
    }
}
