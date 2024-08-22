package de.fraunhofer.iosb.ilt.ams;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.util.Values;

public final class AMS {
    private static final Namespace base =
            new SimpleNamespace("ilt", "https://www.smartfactoryweb.de/ontology/");

    private static final Namespace sfw_playground =
            new SimpleNamespace("iltp", base.getName() + "sfw_playground");

    private static final Namespace sfw_capability_model_top_level =
            new SimpleNamespace("cmtp", base.getName() + "sfw_capability_model_top_level");

    private static final Namespace sfw_capability_model_mid_level =
            new SimpleNamespace("cmmp", base.getName() + "sfw_capability_model_mid_level");

    private static final Namespace sfw_groups_roles =
            new SimpleNamespace("sfwgrp", base.getName() + "sfw_groups_roles");

    private static final Namespace sfw_geo =
            new SimpleNamespace("sfwgeo", base.getName() + "sfw_geo");

    public static final IRI Asset = Values.iri(sfw_capability_model_top_level, "#Asset");
    public static final IRI Capability = Values.iri(sfw_capability_model_top_level, "#Capability");
    public static final IRI Product = Values.iri(sfw_capability_model_top_level, "#Product");
    public static final IRI ProductApplication =
            Values.iri(sfw_capability_model_top_level, "#ProductApplication");
    public static final IRI Entity = Values.iri(sfw_capability_model_top_level, "#Entity");
    public static final IRI Property = Values.iri(sfw_capability_model_top_level, "#Property");
    public static final IRI SemanticReference =
            Values.iri(sfw_capability_model_top_level, "#SemanticReference");
    public static final IRI ProductionResource =
            Values.iri(sfw_capability_model_top_level, "#ProductionResource");
    public static final IRI Machine =
            Values.iri(sfw_capability_model_mid_level, "#MachineResource");
    public static final IRI HumanResource =
            Values.iri(sfw_capability_model_mid_level, "#HumanResource");
    public static final IRI Enterprise = Values.iri(sfw_capability_model_top_level, "#Enterprise");
    public static final IRI Factory = Values.iri(sfw_capability_model_top_level, "#Factory");
    public static final IRI Process = Values.iri(sfw_capability_model_top_level, "#Process");
    public static final IRI VirtualFactory =
            Values.iri(sfw_capability_model_top_level, "#VirtualFactory");
    public static final IRI PhysicalFactory =
            Values.iri(sfw_capability_model_top_level, "#PhysicalFactory");
    public static final IRI ProductClass =
            Values.iri(sfw_capability_model_top_level, "#ProductClass");
    public static final IRI SupplyChain =
            Values.iri(sfw_capability_model_mid_level, "#SupplyChain");
    public static final IRI SupplyChainElement =
            Values.iri(sfw_capability_model_mid_level, "#SupplyChainElement");

    public static final IRI ProductPassport =
            Values.iri(sfw_capability_model_mid_level, "#ProductPassport");
    public static final IRI contains = Values.iri(sfw_capability_model_top_level, "#contains");
    public static final IRI containedIn =
            Values.iri(sfw_capability_model_top_level, "#containedIn");
    public static final IRI has = Values.iri(sfw_capability_model_top_level, "#has");
    public static final IRI generalizes =
            Values.iri(sfw_capability_model_top_level, "#generalizes");
    public static final IRI specializes =
            Values.iri(sfw_capability_model_top_level, "#specializes");

    public static final IRI hasInput = Values.iri(sfw_capability_model_top_level, "#hasInput");
    public static final IRI hasAuxiliaryMaterial =
            Values.iri(sfw_capability_model_mid_level, "#hasAuxiliaryMaterial");
    public static final IRI hasOperatingMaterial =
            Values.iri(sfw_capability_model_mid_level, "#hasOperatingMaterial");
    public static final IRI hasPreliminaryProduct =
            Values.iri(sfw_capability_model_mid_level, "#hasPreliminaryProduct");
    public static final IRI hasRawMaterial =
            Values.iri(sfw_capability_model_mid_level, "#hasRawMaterial");

    public static final IRI hasOutput = Values.iri(sfw_capability_model_top_level, "#hasOutput");
    public static final IRI hasByProduct =
            Values.iri(sfw_capability_model_mid_level, "#hasByProduct");
    public static final IRI hasEndProduct =
            Values.iri(sfw_capability_model_mid_level, "#hasEndProduct");
    public static final IRI hasWasteProduct =
            Values.iri(sfw_capability_model_mid_level, "#hasWasteProduct");

    public static final IRI certificate =
            Values.iri(sfw_capability_model_mid_level, "#certificate");

    public static final IRI hasSemantic =
            Values.iri(sfw_capability_model_top_level, "#hasSemantic");
    public static final IRI identifier = Values.iri(sfw_capability_model_top_level, "#identifier");
    public static final IRI externalIdentifier =
            Values.iri(sfw_capability_model_mid_level, "#externalIdentifier");
    public static final IRI providedBy = Values.iri(sfw_capability_model_top_level, "#providedBy");
    public static final IRI provides = Values.iri(sfw_capability_model_top_level, "#provides");
    public static final IRI realizedBy = Values.iri(sfw_capability_model_top_level, "#realizedBy");
    public static final IRI realizes = Values.iri(sfw_capability_model_top_level, "#realizes");
    public static final IRI requires = Values.iri(sfw_capability_model_top_level, "#requires");
    public static final IRI usedBy = Values.iri(sfw_capability_model_top_level, "#usedBy");
    public static final IRI uses = Values.iri(sfw_capability_model_top_level, "#uses");
    public static final IRI uri = Values.iri(sfw_capability_model_top_level, "#uri");

    public static final IRI managedBy = Values.iri(sfw_groups_roles, "#managedBy");
    public static final IRI manages = Values.iri(sfw_groups_roles, "#manages");

    public static final IRI Location = Values.iri(sfw_geo, "#Location");
    public static final IRI zipcode = Values.iri(sfw_geo, "#zipcode");
    public static final IRI streetNumber = Values.iri(sfw_geo, "#streetNumber");
    public static final IRI street = Values.iri(sfw_geo, "#street");
    public static final IRI longitude = Values.iri(sfw_geo, "#longitude");
    public static final IRI latitude = Values.iri(sfw_geo, "#latitude");
    public static final IRI country = Values.iri(sfw_geo, "#country");
    public static final IRI city = Values.iri(sfw_geo, "#city");

    public static final IRI dn_product_4 = Values.iri(sfw_playground, "dn_product_4");
    public static final IRI possible_dimension_4 =
            Values.iri(sfw_playground, "possible_dimension_4");
    public static final IRI round_bar = Values.iri(sfw_playground, "round_bar");
    public static final IRI sem_ref_2 = Values.iri(sfw_playground, "sem_ref_2");
    public static final IRI sem_ref_1 = Values.iri(sfw_playground, "sem_ref_1");

    public static IRI ofBase(String localName) {
        return Values.iri(base, localName);
    }

    public static IRI ofCapabilityModel(String localName) {
        return Values.iri(sfw_capability_model_top_level, localName);
    }

    public static IRI ofPlayground(String localName) {
        return Values.iri(sfw_playground, localName);
    }

    public static final IRI value = Values.iri(sfw_capability_model_top_level, "#value");
    public static final IRI maxValue = Values.iri(sfw_capability_model_top_level, "#maxValue");
    public static final IRI minValue = Values.iri(sfw_capability_model_top_level, "#minValue");
    public static final IRI logo = Values.iri(sfw_capability_model_top_level, "#logo");
}
