package io.astrodesk.user;


import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;

@Entry(base = "dc=astrodesk", objectClasses = {"inetOrgPerson", "person"})
public class UserEntity {
    @Id
    private Name dn;

    @Attribute(name = "uid")
    private String uid;

    @Attribute(name = "cn")
    private String cn;

    @Attribute(name = "sn")
    private String sn;

    @Attribute(name = "mail")
    private String mail;
    private Boolean isActive;

    private List<String> roles;

    public UserEntity() {}

    public String getUid() {
        return uid;
    }

    public String getCn() {
        return cn;
    }

    public String getMail() {
        return mail;
    }

    public String getUsername() {
        return sn;
    }

    public Boolean getActive() {
        return isActive;
    }

    public List<String> getRoles() {
        return roles;
    }
}
