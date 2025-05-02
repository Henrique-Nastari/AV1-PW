package ufrn.br.lojapw.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ufrn.br.lojapw.model.Produto;
import ufrn.br.lojapw.repository.ProdutoRepository;

import java.io.IOException;
import java.util.List;

@Controller
public class ProdutoController {

    private final ProdutoRepository produtoRepository;

    public ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping("/produtos")
    public void listarProdutos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Produto> produtos = produtoRepository.findAll();

        response.setContentType("text/html;charset=UTF-8");
        var out = response.getWriter();

        out.println("<html><head><title>Lista de Produtos</title></head><body>");
        out.println("<h1>Produtos Disponíveis</h1>");

        // Listando produtos
        out.println("<ul>");
        for (Produto produto : produtos) {
            out.println("<li>");
            out.println(produto.getNome() + " - R$ " + produto.getPreco() + " - Estoque: " + produto.getQuantidade());
            out.println("<form method='post' action='/adicionar-carrinho'>");
            out.println("<input type='hidden' name='produtoId' value='" + produto.getId() + "'/>");
            out.println("<button type='submit'>Adicionar ao Carrinho</button>");
            out.println("</form>");
            out.println("</li><br>");
        }
        out.println("</ul>");

        out.println("<br><a href='/carrinho'>Ver Carrinho</a> | <a href='/logout'>Sair</a>");
        out.println("</body></html>");
    }
}
