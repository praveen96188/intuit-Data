package com.intuit.sbd.payroll.psp.context.helper;

import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Stack;

@Component
public class RequestContextStackHelperImpl implements IRequestContextHelper {

    /**
     * removes the entire requestContextStack from the thread local and initializes with a new stack
     */
    public void resetRequestContext(){
        removeRequestContext();
        initRequestContext();
    }

    /**
     * removes the entire requestContextStack from the thread local
     */
    public void removeRequestContext(){
        RequestAttributesUtils.removeAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE);
    }

    /**
     * initializes thread local with a new requestContextStack
     */
    public void initRequestContext(){
        Stack<RequestContext> requestContextStack = new Stack<>();
        RequestAttributesUtils.setAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE, requestContextStack);
    }

    /**
     * returns requestContextStack from thread local
     * @return requestContextStack
     */
    private Stack<RequestContext> getRequestContextStack(){
        return RequestAttributesUtils.getAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE, Stack.class);
    }

    /**
     * initializes thread local with new requestContextStack if not already initialised
     * pushes request context object in the stack
     * @param requestContext
     */
    public void setCurrentRequestContext(RequestContext requestContext){
        Stack<RequestContext> requestContextStack = getRequestContextStack();
        if(Objects.isNull(requestContextStack)){
            initRequestContext();
            requestContextStack = getRequestContextStack();
        }
        requestContextStack.push(requestContext);
    }

    /**
     * increments the refCount of the top element in the requestContextStack present in the thread local
     * throws exception if thread local not initialized or requestContextStack is empty
     */
    public void incrementCurrentRequestContextRefCount(){
        RequestContext currentRequestContext = getCurrentRequestContext();
        if(Objects.isNull(currentRequestContext)){
            throw new RuntimeException("NullRequestContextException - cannot increment");
        }
        currentRequestContext.getRefCount().incrementAndGet();

    }

    /**
     * returns the top element in the requestContextStack present in the thread local
     * returns null if thread local not initialized or requestContextStack is empty
     * @return
     */
    public RequestContext getCurrentRequestContext(){
        Stack<RequestContext> requestContextStack = getRequestContextStack();
        if(CollectionUtils.isEmpty(requestContextStack)){
            return null;
        }
        return requestContextStack.peek();
    }

    /**
     * decrements the ref count(if ref count > 1) of the top element of the requestContextStack present in the thread local
     * else pops out the top element
     * @return
     */
    public RequestContext clearCurrentRequestContext(){
        Stack<RequestContext> requestContextStack = getRequestContextStack();
        if(CollectionUtils.isEmpty(requestContextStack)){
            return null;
        }

        RequestContext currentRequestContext = requestContextStack.peek();

        if(Objects.nonNull(currentRequestContext) && currentRequestContext.getRefCount().get() > 1){
            currentRequestContext.getRefCount().decrementAndGet();
            return currentRequestContext;
        }

        return requestContextStack.pop();
    }
}
