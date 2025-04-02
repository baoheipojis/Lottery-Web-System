package com.example.lottery.entity;

import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.LocalDateJavaDescriptor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateStringType implements UserType<LocalDate> {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }
    
    @Override
    public Class<LocalDate> returnedClass() {
        return LocalDate.class;
    }
    
    @Override
    public boolean equals(LocalDate x, LocalDate y) {
        return Objects.equals(x, y);
    }
    
    @Override
    public int hashCode(LocalDate x) {
        return Objects.hashCode(x);
    }
    
    @Override
    public LocalDate nullSafeGet(ResultSet rs, int position, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (rs.wasNull() || value == null || value.isEmpty()) {
            return null;
        }
        return LocalDate.parse(value, FORMATTER);
    }
    
    @Override
    public void nullSafeSet(PreparedStatement st, LocalDate value, int index) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, value.format(FORMATTER));
        }
    }
    
    @Override
    public LocalDate deepCopy(LocalDate value) {
        return value;  // LocalDate is immutable
    }
    
    @Override
    public boolean isMutable() {
        return false;  // LocalDate is immutable
    }
    
    @Override
    public Serializable disassemble(LocalDate value) {
        return value == null ? null : value.toString();
    }
    
    @Override
    public LocalDate assemble(Serializable cached, Object owner) {
        return cached == null ? null : LocalDate.parse((String) cached, FORMATTER);
    }

    @Override
    public LocalDate replace(LocalDate detached, LocalDate managed, Object owner) {
        return detached;
    }
}
