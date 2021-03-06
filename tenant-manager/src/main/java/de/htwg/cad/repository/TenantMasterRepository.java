package de.htwg.cad.repository;

import de.htwg.cad.domain.TenantMaster;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@EnableScan
@Repository
public interface TenantMasterRepository extends PagingAndSortingRepository<TenantMaster, String> {
}
