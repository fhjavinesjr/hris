package com.humanresource.saln;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SalnVersionRepository extends JpaRepository<SalnVersion,Long>{List<SalnVersion> findBySalnIdOrderByVersionNoDesc(Long salnId);}
