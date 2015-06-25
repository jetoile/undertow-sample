package org.jboss.resteasy.plugins.providers.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.jboss.resteasy.core.MediaTypeMap;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.*;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * <p>
 *  JSONP is an alternative to normal AJAX requests. Instead of using a XMLHttpRequest a script tag is added to the DOM.
 *  The browser will call the corresponding URL and download the JavaScript. The server creates a response which looks like a
 *  method call. The parameter is the body of the request. The name of the method to call is normally passed as query parameter.
 *  The method has to be present in the current JavaScript environment.
 * </p>
 * <p>
 *  Jackson JSON processor can produce such an response. This interceptor checks if the media type is a JavaScript one if there is a query
 *  parameter with the method name. The default name of this query parameter is "callback". So this interceptor is compatible with 
 *  <a href="http://api.jquery.com/jQuery.ajax/">jQuery</a>.
 * </p>
 *
 * @author <a href="mailto:holger.morch@nokia.com">Holger Morch</a>
 * @version $Revision: 1 $
 */
@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class Jackson2JsonpInterceptor implements WriterInterceptor {

    /**
     * "text/javascript" media type. Default media type of script tags.
     */
    public static final MediaType TEXT_JAVASCRIPT_MEDIA_TYPE = new MediaType("text", "javascript");
    
    /**
     * "application/javascript" media type.
     */
    public static final MediaType APPLICATION_JAVASCRIPT_MEDIA_TYPE = new MediaType("application", "javascript");

    /**
     * "text/json" media type.
     */
    public static final MediaType TEXT_JSON_TYPE = new MediaType("text", "json");

    /**
     * "application/*+json" media type. 
     */
    public static final MediaType APPLICATION_PLUS_JSON_TYPE = new MediaType("application", "*+json");

    /**
     * Default name of the query parameter with the method name.
     */
    public static final String DEFAULT_CALLBACK_QUERY_PARAMETER = "callback";

    /**
     * If response media type is one of this jsonp response may be created.
     */
    public static final MediaTypeMap<String> jsonpCompatibleMediaTypes = new MediaTypeMap<String>();
    
    /**
     * Default {@link ObjectMapper} for type resolution. Used if none is provided by {@link Providers}.
     */
    protected static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
    static {
        DEFAULT_MAPPER.registerModule(new AfterburnerModule());
    }
    
    static {
        jsonpCompatibleMediaTypes.add(MediaType.APPLICATION_JSON_TYPE  , MediaType.APPLICATION_JSON_TYPE.toString());
        jsonpCompatibleMediaTypes.add(APPLICATION_JAVASCRIPT_MEDIA_TYPE, APPLICATION_JAVASCRIPT_MEDIA_TYPE.toString());
        jsonpCompatibleMediaTypes.add(APPLICATION_PLUS_JSON_TYPE       , APPLICATION_PLUS_JSON_TYPE.toString());
        jsonpCompatibleMediaTypes.add(TEXT_JSON_TYPE                   , TEXT_JSON_TYPE.toString());
        jsonpCompatibleMediaTypes.add(TEXT_JAVASCRIPT_MEDIA_TYPE       , TEXT_JAVASCRIPT_MEDIA_TYPE.toString());
    }

    private UriInfo uri;
    
    private String callbackQueryParameter = DEFAULT_CALLBACK_QUERY_PARAMETER;

    /**
     * The {@link ObjectMapper} used to create typing information.
     */
    protected ObjectMapper objectMapper;

    /**
     * The {@link Providers} used to retrieve the {@link #objectMapper} from.
     */
    protected Providers providers;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        String function = uri.getQueryParameters().getFirst(callbackQueryParameter);
        if (function != null && !function.trim().isEmpty() && !jsonpCompatibleMediaTypes.getPossible(context.getMediaType()).isEmpty()){
            OutputStreamWriter writer = new OutputStreamWriter(context.getOutputStream());
            
            writer.write(function + "(");
            writer.flush();
            context.proceed();
            writer.write(")");
            writer.flush();
        } else {
            context.proceed();
        }
    }
    
    /**
     * Search for an {@link ObjectMapper} for the given class and mediaType
     * 
     * @param type the {@link Class} to serialize
     * @param mediaType the response {@link MediaType}
     * @return the {@link ObjectMapper}
     */
    protected ObjectMapper getObjectMapper(Class<?> type, MediaType mediaType)
    {
        if (objectMapper != null) {
            return objectMapper;
        }

        if (providers != null) {
            ContextResolver<ObjectMapper> resolver = providers.getContextResolver(ObjectMapper.class, mediaType);
            if (resolver == null) {
                resolver = providers.getContextResolver(ObjectMapper.class, null);
            }
            if (resolver != null) {
                return resolver.getContext(type);
            }
        }
        
        return DEFAULT_MAPPER;
    }
    
    
    /**
     * Setter used by RESTeasy to provide the {@link UriInfo}.
     * 
     * @param uri the uri to set
     */
    @Context
    public void setUri(UriInfo uri) {
        this.uri = uri;
    }
    
    /**
     * Setter used by RESTeasy to provide the {@link Providers}
     * 
     * @param providers
     */
    @Context
    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    /**
     * Set an fix {@link ObjectMapper}. If this is not set {@link Providers} are used for lookup. If there are is none too, use a default one.
     * 
     * @param objectMapper
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Get the name of the query parameter which contains the JavaScript method name. Default: callback.
     * 
     * @return the callbackQueryParameter
     */
    public String getCallbackQueryParameter() {
        return callbackQueryParameter;
    }

    /**
     * Set callback query parameter.
     * 
     * @see #getCallbackQueryParameter()
     * @param callbackQueryParameter the callbackQueryParameter to set
     */
    public void setCallbackQueryParameter(String callbackQueryParameter) {
        this.callbackQueryParameter = callbackQueryParameter;
    }

}