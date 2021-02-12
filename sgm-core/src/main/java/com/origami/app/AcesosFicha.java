/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.app;

import com.origami.session.UserSession;
import com.origami.sgm.database.SchemasConfig;
import com.origami.sgm.database.SchemasConfigOracle;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Angel Navarro
 */
@Named
@ApplicationScoped
public class AcesosFicha {

    private final Long tecnico_catastro = 66l;
    private final Long director_catastro = 68l;
    private final Long verificador = 174l;
    private final String EDITAR = "editar";
    private final String IMPRIMIR = "imprimir";
    private final String TRANSFERENCIA = "transferencia";
    private final String HISTORICO = "historico";

    @Inject
    private UserSession session;

    public Boolean ver() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
                if (session.getRoles().contains(tecnico_catastro)) {
                    return true;
                } else if (session.getRoles().contains(director_catastro)) {
                    return true;
                } else if (session.getRoles().contains(verificador)) {
                    return true;
                }
            } else { // Solo ver los otros departamentos...
                return true;
            }
        }
        return false;
    }

    public Boolean editar() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
//                if (session.getRoles().contains(tecnico_catastro)) {
//                    return true;
//                } else if (session.getRoles().contains(director_catastro)) {
//                    return true;
//                } else if (session.getRoles().contains(verificador)) {
//                    return true;
//                }
                return session.getOpcionesFicha().contains(EDITAR);
            } else {
                return false;
            }
        }
    }

    public Boolean imprimir() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
//                if (session.getRoles().contains(tecnico_catastro)) {
//                    return true;
//                } else if (session.getRoles().contains(director_catastro)) {
//                    return true;
//                } else if (session.getRoles().contains(verificador)) {
//                    return false;
//                }
                return session.getOpcionesFicha().contains(IMPRIMIR);
            } else {
                return false;
            }
        }
    }

    public Boolean historico() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
//                if (session.getRoles().contains(tecnico_catastro)) {
//                    return true;
//                } else if (session.getRoles().contains(director_catastro)) {
//                    return true;
//                }
                return session.getOpcionesFicha().contains(HISTORICO);
            } else {
                return false;
            }
        }
    }

    public Boolean dividir() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
                if (session.getRoles().contains(tecnico_catastro)) {
                    return true;
                } else if (session.getRoles().contains(director_catastro)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public Boolean fusionar() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
                if (session.getRoles().contains(tecnico_catastro)) {
                    return true;
                } else if (session.getRoles().contains(director_catastro)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public Boolean eliminar() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
                if (session.getRoles().contains(tecnico_catastro)) {
                    return true;
                } else if (session.getRoles().contains(director_catastro)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public Boolean cession() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
                if (session.getRoles().contains(tecnico_catastro)) {
                    return true;
                } else if (session.getRoles().contains(director_catastro)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public Boolean trasnferenciaDominio() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
//                if (session.getRoles().contains(tecnico_catastro)) {
//                    return true;
//                } else if (session.getRoles().contains(director_catastro)) {
//                    return false;
//                }
                return session.getOpcionesFicha().contains(TRANSFERENCIA);
            } else {
                return false;
            }
        }
    }

    public Boolean esOracle() {
        return SchemasConfig.DB_ENGINE == SchemasConfigOracle.DB_ENGINE.ORACLE;
    }

    public Boolean actualizarConstruccion() {
        if (session.getIsAdmin()) {
            return true;
        } else {
            if (session.getDepts().contains(2L)) {
                if (session.getRoles().contains(tecnico_catastro)) {
                    return true;
                } else if (session.getRoles().contains(director_catastro)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean getEsCatastro() {
        return session.getDepts().contains(2L) || session.getIsAdmin();
    }

}
