package ifml2.vm;

import ifml2.om.Attribute;
import ifml2.om.RoleDefinition;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ISymbolResolver
{
    Value resolveSymbol(@NotNull String symbol) throws IFML2VMException;

    List<Attribute> getAttributeList();

    List<RoleDefinition> getRoleDefinitionList();
}
