# Uzi Blog. Version 1.2

Custom written blog using JEE web profile. 

**Tested with:**
* Wildfly 8.2
* PostgreSQL 9.4
* Primefaces 5.2

**Build:**
mvn package

## Set up
**Database (PostgreSQL) Set up**
1. Create database 'blog'.
2. Run schema.sql (and data.sql) from /schema in blog database.

**Wildfly 8.2 Set up** 
1. Create postgresql module in Wildfly:

* Download and copy `postgresql-9.4-1201.jdbc41.jar` to `<wildfly>/modules/org/postgresql/main/postgresql-9.4-1201.jdbc41.jar`
* Create `<wildfly>/modules/org/postgresql/main/module.xml`:
```
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.1" name="org.postgresql">
     <resources>
         <resource-root path="postgresql-9.4-1201.jdbc41.jar"/>
     </resources>
     <dependencies>
         <module name="javax.api"/>
         <module name="javax.transaction.api"/>
         <module name="javax.servlet.api" optional="true"/>
     </dependencies>
</module>
```

2. Create datasource and driver in `<wildfly>/standalone/configuration/standalone.xml` (Update connection URL, user and password)
```
            <datasources>
                ...			
                 <datasource jta="true" jndi-name="java:/uziblogds" pool-name="uziblogds" enabled="true" use-java-context="true" use-ccm="true">
                    <connection-url>jdbc:postgresql://somehost.ap-southeast-2.rds.amazonaws.com:5432/blog</connection-url>
                    <driver>postgresql</driver>
                    <security>
                        <user-name>user</user-name>
                        <password>password</password>
                    </security>
                    <validation>
                        <check-valid-connection-sql>SELECT 1</check-valid-connection-sql>
                    </validation>
                </datasource>
                <drivers>
                    ...
                    <driver name="postgresql" module="org.postgresql">
                        <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
                    </driver>
                </drivers>
            </datasources>

```

3. Create security context in standalone.xml
```
       <subsystem xmlns="urn:jboss:domain:security:1.2">
            <security-domains>
                ...
                <security-domain name="uziblog" cache-type="default">
                    <authentication>
                        <login-module code="UsersRoles" flag="required">
                            <module-option name="usersProperties" value="${jboss.server.config.dir}/uziblog-users.properties"/>
                            <module-option name="rolesProperties" value="${jboss.server.config.dir}/uziblog-roles.properties"/>
                        </login-module>
                    </authentication>
                </security-domain>
```

4. Create user roles configuration files in `<wildfly>/standalone/configuration/` (Update user/password to your choice): 

uziblog-users.properties:
~~~
user=password
~~~
uziblog-roles.properties:
~~~
user=blogadmin
~~~

### Enabling SSL in Wildfly

The following instructions assume that you own a domain and running Wildfly in Amazon EC2 environment with direct host access.

* Pre-steps *
1. Generate CSR to request an SSL certificate from SSL provider (such as ssls.com or globessl.com)
`openssl req -new -newkey rsa:2048 -nodes -keyout yourdomain.com.key -out yourdomain.com.csr`
2. Use the generated CSR to purchase a certificate.
3. Merge all root and intermediate certificates in one file (provided by SSL Vendor) if required.
```
cat COMODORSADomainValidationSecureServerCA.crt COMODORSAAddTrustCA.crt AddTrustExternalCARoot.crt   > CAChainMerged.crt
```
4. Export in pkcs12 format (yourdomain_com.crt is provided by SSL vendor).
```
openssl pkcs12 -export -out yourdomain.p12 -inkey yourdomain.com.key -in yourdomain_com.crt -name AnyFriendlyAliasYouWantToSpecify -chain -CAfile CAChainMerged.crt
```

* Wildfly setup *
1. Place yourdomain.p12 in `<wildfly>/standalone/configuration/`
2. Modify ``<wildfly>/standalone/configuration/standalone.xml`
2.1 Create security realm:
~~~
        <security-realms>
        ...
            <security-realm name="UndertowRealm">
                <server-identities>
                    <ssl>
                        <keystore provider="pkcs12" path="yourdomain.p12" relative-to="jboss.server.config.dir" keystore-password="SomePassword" alias="AnyFriendlyAliasYouWantToSpecify" key-password="SomePassword"/>
                    </ssl>
                </server-identities>
            </security-realm>
        </security-realms>
~~~
2.2 Modify undertow subsystem to add https-listener
~~~
        <subsystem xmlns="urn:jboss:domain:undertow:1.2">
            <buffer-cache name="default"/>
            <server name="default-server">
                <http-listener name="default" socket-binding="http"/>
                <https-listener name="defaulthttps" socket-binding="https" security-realm="UndertowRealm"/>
				...
~~~
2.3 Make sure https socket binding is enabled
~~~
    <socket-binding-group name="standard-sockets" default-interface="public" port-offset="${jboss.socket.binding.port-offset:0}">
        ...
        <socket-binding name="https" port="${jboss.https.port:8443}"/>
		...
~~~

## Deployment
Deploy as ROOT.war in standalone/deployment.

## Re-routing 80 and 443 to 8080 and 8443 respectively (assuming EC2 environment and respective security groups are already configured)
~~~
sudo /sbin/iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
sudo /sbin/iptables -t nat -I PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 8443
service iptables save
~~~


 


