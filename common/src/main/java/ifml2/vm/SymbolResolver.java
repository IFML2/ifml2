package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.om.Attribute;
import ifml2.om.RoleDefinition;
import ifml2.vm.values.Value;

import java.util.List;

public interface SymbolResolver {
    Value resolveSymbol(String symbol) throws IFML2VMException;

    List<Attribute> getAttributeList();

    List<RoleDefinition> getRoleDefinitionList();
}
