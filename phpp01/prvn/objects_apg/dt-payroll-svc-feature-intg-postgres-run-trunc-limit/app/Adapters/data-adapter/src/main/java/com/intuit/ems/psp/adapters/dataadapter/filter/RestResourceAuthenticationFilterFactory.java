package com.intuit.ems.psp.adapters.dataadapter.filter;

import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charithah418 on 11/11/15.
 */

@Provider
public class RestResourceAuthenticationFilterFactory extends RolesAllowedResourceFilterFactory {


    private final RestResourceAuthenticationFilter mRestResourceAuthenticationFilter = new RestResourceAuthenticationFilter();

    @Override
    public List<ResourceFilter> create(AbstractMethod mAbstractMethod) {
        List<ResourceFilter> filters = null;
        if (mAbstractMethod.isAnnotationPresent(RolesAllowed.class)) {
            List<ResourceFilter> rolesFilters = super.create(mAbstractMethod);
            if (null == rolesFilters) {
                rolesFilters = new ArrayList<ResourceFilter>();
            }

            filters = new ArrayList<ResourceFilter>(rolesFilters);

            filters.add(0, mRestResourceAuthenticationFilter);
        }
        return filters;
    }

}
