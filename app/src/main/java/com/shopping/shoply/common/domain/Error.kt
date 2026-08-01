package com.shopping.shoply.common.domain

// Open marker (not sealed): each feature's domain defines its own typed errors
// (DataError here, ValidationError in auth) and they all ride in Result<D, E : Error>.
// Sealed would confine every implementer to this package, blocking that.
interface Error