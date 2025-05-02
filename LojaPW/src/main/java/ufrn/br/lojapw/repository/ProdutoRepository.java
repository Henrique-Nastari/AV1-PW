package ufrn.br.lojapw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ufrn.br.lojapw.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

}
