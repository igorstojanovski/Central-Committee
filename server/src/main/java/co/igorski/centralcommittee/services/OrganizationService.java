package co.igorski.centralcommittee.services;

import co.igorski.centralcommittee.model.Organization;
import co.igorski.centralcommittee.repositories.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    public Organization createOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }

    public Optional<Organization> getOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId);
    }

    public List<Organization> getOrganizations() {
        List<Organization> organizations = new ArrayList<>();
        organizationRepository.findAll().forEach(organizations::add);

        return organizations;
    }
}
