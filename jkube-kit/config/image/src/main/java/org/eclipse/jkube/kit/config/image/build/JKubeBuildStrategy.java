/*
 * Copyright (c) 2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at:
 *
 *     https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.jkube.kit.config.image.build;

import lombok.Getter;

/**
 * OpenShift build mode. Only used when mode is "openshift"
 *
 * @author roland
 * @since 01/08/16
 */
@Getter
public enum JKubeBuildStrategy {

    // Constants used to extract extra information from a `fromExt` build configuration
    /**
     * S2i build with a binary source
     */
    s2i("S2I", true),

    /**
     * JIB build
     */
    jib("Jib", true),

    /**
     * Docker build with a binary source
     */
    docker("Docker", true),

    /**
     * BuildPacks
     */
    buildpacks("Buildpacks", false);

    // Source strategy elements
    public enum SourceStrategy {
        kind,
        namespace,
        name;

        public String key() {
            // Return the name, could be mapped if needed.
            return name();
        }
    }


    private final String label;
    private final boolean supportsWatch;

    JKubeBuildStrategy(String label, boolean supportsWatch) {
        this.label = label;
        this.supportsWatch = supportsWatch;
    }

    /**
     * Check if the given type is same as the type stored in OpenShift
     *
     * @param type to check
     * @return boolean value whether type is same or not.
     */
    public boolean isSame(String type) {
        return type != null && (
            (type.equalsIgnoreCase("source") && this == s2i) ||
            (type.equalsIgnoreCase("docker") && this == docker)
        );
    }

}

