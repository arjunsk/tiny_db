package com.arjunsk.tiny_db.server.a_frontend.common.domain.clause;

import java.io.Serializable;

/**
 * The class that denotes values stored in the database.
 *
 * @author Edward Sciore
 */
public class D_Constant implements Comparable<D_Constant> , Serializable {
    private static final long serialVersionUID = 1L;

    private Integer ival = null;
    private String sval = null;

    public D_Constant(Integer ival) {
        this.ival = ival;
    }

    public D_Constant(String sval) {
        this.sval = sval;
    }

    public Integer asInt() {
        return ival;
    }

    public String asString() {
        return sval;
    }

    public boolean equals(Object obj) {
        D_Constant c = (D_Constant) obj;
        return (ival != null) ? ival.equals(c.ival) : sval.equals(c.sval);
    }

    public int compareTo(D_Constant c) {
        return (ival != null) ? ival.compareTo(c.ival) : sval.compareTo(c.sval);
    }

    public int hashCode() {
        return (ival != null) ? ival.hashCode() : sval.hashCode();
    }

    public String toString() {
        return (ival != null) ? ival.toString() : sval.toString();
    }
}
