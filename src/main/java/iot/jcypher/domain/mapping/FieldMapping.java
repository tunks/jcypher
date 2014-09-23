/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package iot.jcypher.domain.mapping;

import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

public class FieldMapping {

	private Field field;
	private String fieldName;
	private String propertyName;
	private String classFieldName;
	
	public FieldMapping(Field field) {
		this(field, field.getName());
	}
	
	public FieldMapping(Field field, String propertyName) {
		super();
		this.field = field;
		this.field.setAccessible(true);
		this.propertyName = propertyName;
	}

	public void mapPropertyFromField(Object domainObject, GrNode rNode) {
		try {
			prepare(domainObject);
			
			if (getObjectNeedingRelation(domainObject) == null) { // also checks against DomainInfo
				// we can map to a property
				Object value = this.field.get(domainObject);
				value = MappingUtil.convertToProperty(value);
				GrProperty prop = rNode.getProperty(this.propertyName);
				if (value != null) {
					if (prop != null) {
						Object propValue = MappingUtil.convertFromProperty(prop.getValue(), value.getClass(),
								getListComponentType());
						if (!propValue.equals(value)) {
							prop.setValue(value);
						}
					} else
						rNode.addProperty(this.propertyName, value);
				} else {
					if (prop != null)
						prop.setValue(null);
				}
			} else {
				// a previously empty collection might hav been mapped to a property
				// we need to remove the property
				if (Collection.class.isAssignableFrom(getFieldType())) {
					GrProperty prop = rNode.getProperty(this.propertyName);
					if (prop != null)
						prop.setValue(null);
				}
			}
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void mapPropertyToField(Object domainObject, GrNode rNode) {
		try {
			prepare(domainObject);
			
			Object value = this.field.get(domainObject);
			GrProperty prop = rNode.getProperty(this.propertyName);
			if (prop != null) {
				Object propValue = prop.getValue();
				if (propValue != null) { // allow null values in properties
					Class<?> typ = this.field.getType();
					propValue = MappingUtil.convertFromProperty(propValue, typ,
							getListComponentType());
					if (!propValue.equals(value)) {
						this.field.set(domainObject, propValue);
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setField(Object domainObject, Object value) {
		try {
			prepare(domainObject);
			this.field.set(domainObject, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * @return the value of the field, if this value cannot be mapped to a property,
	 * but must be mapped to a seperate node connected via a relation, else return null.
	 */
	@SuppressWarnings("rawtypes")
	public Object getObjectNeedingRelation(Object domainObject) {
		Object value = null;
		try {
			prepare(domainObject);
			if (needsRelation()) { // also checks against DominInfo
				value = this.field.get(domainObject);
				if (value != null) {
					// store concrete type in DomainInfo
					String classField = getClassFieldName();
					MappingUtil.internalDomainAccess.get()
						.addConcreteFieldType(classField, value.getClass());
					
					// check for list (collection) containing primitive or simple types
					if (Collection.class.isAssignableFrom(this.field.getType())) {
						Collection coll = (Collection) this.field.getType().cast(value);
						if (coll.size() > 0) {
							Object elem = coll.iterator().next();
							Class<?> type = elem.getClass();
							// store that info in DomainInfo
							classField = getClassFieldName();
							MappingUtil.internalDomainAccess.get()
								.addFieldComponentType(classField, type);
							// test the first element,
							// assuming all elements are of the same type !!!
							if (MappingUtil.isSimpleType(type)) { // elements are of primitive or simple type
								// to return null
								value = null;
							}
						} else { // empty lists are mapped to a property
							value = null;
						}
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return value;
	}
	
	/**
	 * 
	 * @return true, if this field cannot be mapped to a property,
	 * but must be mapped to a seperate node connected via a relation, else return false.
	 */
	public boolean needsRelation() {
		boolean ret = !MappingUtil.mapsToProperty(this.field.getGenericType());
		if (ret) { // check for list (collection) containing primitive or simple types
						// in DomainInfo
			if (Collection.class.isAssignableFrom(this.field.getType())) {
				String classField = getClassFieldName();
				CompoundObjectType cType = MappingUtil.internalDomainAccess.get()
					.getFieldComponentType(classField);
				// if cType == null, false will be returned
				if (cType != null) {
					ret = !MappingUtil.mapsToProperty(cType.getType());
				} else
					ret = true; // cannot determine if the component type is simple
									   // so return true and leave the decission for later,
									   // when a concrete component is available
			}
		}
		return ret;
	}
	
	/**
	 * only called when to check for a concrete simple component type
	 * @return
	 */
	private Class<?> getListComponentType() {
		Type typ = MappingUtil.getListComponentType(this.field.getGenericType());
		if (typ == null) { // check for list (collection) containing primitive or simple types
										// in DomainInfo
			if (isCollection()) {
				String classField = getClassFieldName();
				CompoundObjectType cType = MappingUtil.internalDomainAccess.get()
					.getFieldComponentType(classField);
				if (cType != null)
					return cType.getType();
			}
		} else if (typ instanceof Class<?>)
			return (Class<?>)typ;
		return null;
	}
	
	public String getPropertyOrRelationName() {
		return this.propertyName;
	}
	
	public Class<?> getFieldType () {
		return this.field.getType();
	}
	
	public String getFieldName() {
		if (this.fieldName == null)
			this.fieldName = this.field.getName();
		return this.fieldName;
	}

	private void prepare(Object domainObject) throws NoSuchFieldException, SecurityException {
		if (this.fieldName == null)
			this.fieldName = this.field.getName();
	}
	
	public String getClassFieldName() {
		if (this.classFieldName == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(this.field.getDeclaringClass().getName());
			sb.append('_');
			sb.append(this.field.getName());
			this.classFieldName = sb.toString();
		}
		return this.classFieldName;
	}

	public boolean isCollection() {
		return Collection.class.isAssignableFrom(this.field.getType());
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldMapping other = (FieldMapping) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}
	
}