/*
 * YARN Simplified API layer for services
 * Bringing a new service on YARN today is not a simple experience. The APIs of existing frameworks are either too low level (native YARN), require writing new code (for frameworks with programmatic APIs) or writing a complex spec (for declarative frameworks).  This simplified REST API can be used to create and manage the lifecycle of YARN services. In most cases, the application owner will not be forced to make any changes to their applications. This is primarily true if the application is packaged with containerization technologies like Docker.  This document describes the API specifications (aka. YarnFile) for deploying/managing containerized services on YARN. The same JSON spec can be used for both REST API and CLI to manage the services.
 *
 * OpenAPI spec version: 1.0.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package org.apache.cb.yarn.service.api.records;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The kerberos principal info of the user who launches the service.
 */
@ApiModel(description = "The kerberos principal info of the user who launches the service.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-08-08T16:59:40.572+02:00")
public class KerberosPrincipal {
    @JsonProperty("principal_name")
    private String principalName = null;

    @JsonProperty("keytab")
    private String keytab = null;

    public KerberosPrincipal principalName(String principalName) {
        this.principalName = principalName;
        return this;
    }

    /**
     * The principal name of the user who launches the service. Note that &#x60;_HOST&#x60; is required in the &#x60;principal_name&#x60; field such as &#x60;testuser/_HOST@EXAMPLE.COM&#x60; because Hadoop client validates that the server&#39;s (in this case, the AM&#39;s) principal has hostname present when communicating to the server.
     *
     * @return principalName
     **/
    @ApiModelProperty(value = "The principal name of the user who launches the service. Note that `_HOST` is required in the `principal_name` field such as `testuser/_HOST@EXAMPLE.COM` because Hadoop client validates that the server's (in this case, the AM's) principal has hostname present when communicating to the server.")
    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public KerberosPrincipal keytab(String keytab) {
        this.keytab = keytab;
        return this;
    }

    /**
     * The URI of the kerberos keytab. Currently supports only files present on the bare host. URI starts with \&quot;file\\://\&quot; - A path on the local host where the keytab is stored. It is assumed that admin pre-installs the keytabs on the local host before AM launches.
     *
     * @return keytab
     **/
    @ApiModelProperty(value = "The URI of the kerberos keytab. Currently supports only files present on the bare host. URI starts with \"file\\://\" - A path on the local host where the keytab is stored. It is assumed that admin pre-installs the keytabs on the local host before AM launches.")
    public String getKeytab() {
        return keytab;
    }

    public void setKeytab(String keytab) {
        this.keytab = keytab;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KerberosPrincipal kerberosPrincipal = (KerberosPrincipal) o;
        return Objects.equals(this.principalName, kerberosPrincipal.principalName) &&
                Objects.equals(this.keytab, kerberosPrincipal.keytab);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principalName, keytab);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class KerberosPrincipal {\n");

        sb.append("    principalName: ").append(toIndentedString(principalName)).append("\n");
        sb.append("    keytab: ").append(toIndentedString(keytab)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

