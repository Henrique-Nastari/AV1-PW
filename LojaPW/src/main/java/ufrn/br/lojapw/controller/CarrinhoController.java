package ufrn.br.lojapw.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ufrn.br.lojapw.model.Produto;
import ufrn.br.lojapw.repository.ProdutoRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CarrinhoController {

    private final ProdutoRepository produtoRepository;

    public CarrinhoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @PostMapping("/adicionar-carrinho")
    public String adicionarCarrinho(HttpServletRequest request, HttpServletResponse response) {
        Long produtoId = Long.valueOf(request.getParameter("produtoId"));

        Produto produto = produtoRepository.findById(produtoId).orElse(null);
        if (produto == null) {
            return "redirect:/produtos";
        }

        HttpSession session = request.getSession();
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }

        carrinho.add(produto);
        session.setAttribute("carrinho", carrinho);

        salvarCarrinhoNoCookie(response, carrinho);

        return "redirect:/produtos";
    }

    @GetMapping("/carrinho")
    public void visualizarCarrinho(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");

        if (carrinho == null) {
            // Se não existir carrinho na sessão, tenta recuperar do cookie
            carrinho = recuperarCarrinhoDoCookie(request);
            session.setAttribute("carrinho", carrinho);
        }

        response.setContentType("text/html;charset=UTF-8");
        var out = response.getWriter();

        out.println("<html><head><title>Carrinho de Compras</title></head><body>");
        out.println("<h1>Seu Carrinho</h1>");

        if (carrinho.isEmpty()) {
            out.println("<p>Carrinho vazio!</p>");
        } else {
            out.println("<ul>");
            for (Produto produto : carrinho) {
                out.println("<li>");
                out.println(produto.getNome() + " - R$ " + produto.getPreco());
                out.println("<form method='post' action='/remover-carrinho'>");
                out.println("<input type='hidden' name='produtoId' value='" + produto.getId() + "'/>");
                out.println("<button type='submit'>Remover</button>");
                out.println("</form>");
                out.println("</li><br>");
            }
            out.println("</ul>");

            // Botão para finalizar compra
            out.println("<br><form method='post' action='/finalizar-compra'>");
            out.println("<button type='submit'>Finalizar Compra</button>");
            out.println("</form>");
        }

        out.println("<br><a href='/produtos'>Continuar comprando</a>");
        out.println("</body></html>");
    }


    @PostMapping("/remover-carrinho")
    public String removerCarrinho(HttpServletRequest request, HttpServletResponse response) {
        Long produtoId = Long.valueOf(request.getParameter("produtoId"));

        HttpSession session = request.getSession();
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");

        if (carrinho != null) {
            carrinho.removeIf(produto -> produto.getId().equals(produtoId));
            session.setAttribute("carrinho", carrinho);

            salvarCarrinhoNoCookie(response, carrinho);
        }

        return "redirect:/carrinho";
    }

    @PostMapping("/finalizar-compra")
    public void finalizarCompra(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");

        if (carrinho == null || carrinho.isEmpty()) {
            response.sendRedirect("/carrinho");
            return;
        }

        double totalCompra = 0.0;

        for (Produto produtoCarrinho : carrinho) {
            Produto produtoBanco = produtoRepository.findById(produtoCarrinho.getId()).orElse(null);

            if (produtoBanco != null && produtoBanco.getQuantidade() > 0) {
                produtoBanco.setQuantidade(produtoBanco.getQuantidade() - 1);
                produtoRepository.save(produtoBanco);

                totalCompra += produtoBanco.getPreco();
            }
        }

        session.removeAttribute("carrinho");
        limparCarrinhoCookie(response);

        response.setContentType("text/html;charset=UTF-8");
        var out = response.getWriter();
        out.println("<html><head><title>Compra Finalizada</title></head><body>");
        out.println("<h1>Compra Finalizada com Sucesso!</h1>");
        out.println("<p>Valor total: R$ " + String.format("%.2f", totalCompra) + "</p>");
        out.println("<br><a href='/produtos'>Voltar para produtos</a>");
        out.println("</body></html>");
    }

    // Métodos auxiliares
    private void salvarCarrinhoNoCookie(HttpServletResponse response, List<Produto> carrinho) {
        StringBuilder ids = new StringBuilder();
        for (Produto produto : carrinho) {
            if (ids.length() > 0) {
                ids.append(",");
            }
            ids.append(produto.getId());
        }

        Cookie cookie = new Cookie("carrinho", ids.toString());
        cookie.setMaxAge(20 * 60); // 20 minutos
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void limparCarrinhoCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("carrinho", "");
        cookie.setMaxAge(0); // Expira imediatamente
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private List<Produto> recuperarCarrinhoDoCookie(HttpServletRequest request) {
        List<Produto> carrinho = new ArrayList<>();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("carrinho".equals(cookie.getName())) {
                    String valor = cookie.getValue(); // ex: "1,2,5"
                    if (!valor.isEmpty()) {
                        String[] ids = valor.split(",");
                        for (String idStr : ids) {
                            try {
                                Long id = Long.valueOf(idStr);
                                Produto produto = produtoRepository.findById(id).orElse(null);
                                if (produto != null) {
                                    carrinho.add(produto);
                                }
                            } catch (NumberFormatException e) {
                                // Ignora IDs inválidos
                            }
                        }
                    }
                }
            }
        }

        return carrinho;
    }
}
