package eu.unitn.disi.db.command.util;

import static java.lang.String.format;

/*
 * IQR (Interactive Query Relaxation) Library
 * Copyright (C) 2011  Davide Mottin (mottin@disi.unitn.eu
 * Alice Marascu (marascu@disi.unitn.eu)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/



/**
 * This class represent a simple Pair of two elements
 * @author Davide Mottin
 */
public class Pair<A,B> {
    protected A first;
    protected B second;

    public Pair() {}

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return format("(%s,%s)", first, second);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Pair && first != null && second != null) {
            return first.equals(((Pair)obj).first) && second.equals(((Pair)obj).second);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.first != null ? this.first.hashCode() : 0);
        hash = 11 * hash + (this.second != null ? this.second.hashCode() : 0);
        return hash;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Pair<>(first, second);
    }
}
