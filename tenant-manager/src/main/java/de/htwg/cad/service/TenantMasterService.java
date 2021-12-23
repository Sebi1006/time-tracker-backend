package de.htwg.cad.service;

import de.htwg.cad.domain.TenantMaster;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface TenantMasterService {
    /**
     * Get tenant info by id
     *
     * @param tenantId tenant id
     * @return {{@link TenantMaster}}
     */
    Optional<TenantMaster> getTenantById(@NotNull String tenantId);
}
