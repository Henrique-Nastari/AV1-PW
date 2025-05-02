package ufrn.br.lojapw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ufrn.br.lojapw.model.Lojista;

public interface LojistaRepository extends JpaRepository<Lojista, Long> {
    Lojista findByEmail(String email);
}
