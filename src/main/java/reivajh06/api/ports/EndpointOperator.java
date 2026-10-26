package reivajh06.api.ports;

import reivajh06.api.ApiException;

public interface EndpointOperator<I, O> {

	O operate(I input) throws ApiException;
}
