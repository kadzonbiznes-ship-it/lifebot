/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import java.util.Set;
import java.util.function.IntFunction;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Checks;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.ThreadLocalUtil;

public final class GLCapabilities {
    static final int ADDRESS_BUFFER_SIZE = 2228;
    public final long glEnable;
    public final long glDisable;
    public final long glAccum;
    public final long glAlphaFunc;
    public final long glAreTexturesResident;
    public final long glArrayElement;
    public final long glBegin;
    public final long glBindTexture;
    public final long glBitmap;
    public final long glBlendFunc;
    public final long glCallList;
    public final long glCallLists;
    public final long glClear;
    public final long glClearAccum;
    public final long glClearColor;
    public final long glClearDepth;
    public final long glClearIndex;
    public final long glClearStencil;
    public final long glClipPlane;
    public final long glColor3b;
    public final long glColor3s;
    public final long glColor3i;
    public final long glColor3f;
    public final long glColor3d;
    public final long glColor3ub;
    public final long glColor3us;
    public final long glColor3ui;
    public final long glColor3bv;
    public final long glColor3sv;
    public final long glColor3iv;
    public final long glColor3fv;
    public final long glColor3dv;
    public final long glColor3ubv;
    public final long glColor3usv;
    public final long glColor3uiv;
    public final long glColor4b;
    public final long glColor4s;
    public final long glColor4i;
    public final long glColor4f;
    public final long glColor4d;
    public final long glColor4ub;
    public final long glColor4us;
    public final long glColor4ui;
    public final long glColor4bv;
    public final long glColor4sv;
    public final long glColor4iv;
    public final long glColor4fv;
    public final long glColor4dv;
    public final long glColor4ubv;
    public final long glColor4usv;
    public final long glColor4uiv;
    public final long glColorMask;
    public final long glColorMaterial;
    public final long glColorPointer;
    public final long glCopyPixels;
    public final long glCullFace;
    public final long glDeleteLists;
    public final long glDepthFunc;
    public final long glDepthMask;
    public final long glDepthRange;
    public final long glDisableClientState;
    public final long glDrawArrays;
    public final long glDrawBuffer;
    public final long glDrawElements;
    public final long glDrawPixels;
    public final long glEdgeFlag;
    public final long glEdgeFlagv;
    public final long glEdgeFlagPointer;
    public final long glEnableClientState;
    public final long glEnd;
    public final long glEvalCoord1f;
    public final long glEvalCoord1fv;
    public final long glEvalCoord1d;
    public final long glEvalCoord1dv;
    public final long glEvalCoord2f;
    public final long glEvalCoord2fv;
    public final long glEvalCoord2d;
    public final long glEvalCoord2dv;
    public final long glEvalMesh1;
    public final long glEvalMesh2;
    public final long glEvalPoint1;
    public final long glEvalPoint2;
    public final long glFeedbackBuffer;
    public final long glFinish;
    public final long glFlush;
    public final long glFogi;
    public final long glFogiv;
    public final long glFogf;
    public final long glFogfv;
    public final long glFrontFace;
    public final long glGenLists;
    public final long glGenTextures;
    public final long glDeleteTextures;
    public final long glGetClipPlane;
    public final long glGetBooleanv;
    public final long glGetFloatv;
    public final long glGetIntegerv;
    public final long glGetDoublev;
    public final long glGetError;
    public final long glGetLightiv;
    public final long glGetLightfv;
    public final long glGetMapiv;
    public final long glGetMapfv;
    public final long glGetMapdv;
    public final long glGetMaterialiv;
    public final long glGetMaterialfv;
    public final long glGetPixelMapfv;
    public final long glGetPixelMapusv;
    public final long glGetPixelMapuiv;
    public final long glGetPointerv;
    public final long glGetPolygonStipple;
    public final long glGetString;
    public final long glGetTexEnviv;
    public final long glGetTexEnvfv;
    public final long glGetTexGeniv;
    public final long glGetTexGenfv;
    public final long glGetTexGendv;
    public final long glGetTexImage;
    public final long glGetTexLevelParameteriv;
    public final long glGetTexLevelParameterfv;
    public final long glGetTexParameteriv;
    public final long glGetTexParameterfv;
    public final long glHint;
    public final long glIndexi;
    public final long glIndexub;
    public final long glIndexs;
    public final long glIndexf;
    public final long glIndexd;
    public final long glIndexiv;
    public final long glIndexubv;
    public final long glIndexsv;
    public final long glIndexfv;
    public final long glIndexdv;
    public final long glIndexMask;
    public final long glIndexPointer;
    public final long glInitNames;
    public final long glInterleavedArrays;
    public final long glIsEnabled;
    public final long glIsList;
    public final long glIsTexture;
    public final long glLightModeli;
    public final long glLightModelf;
    public final long glLightModeliv;
    public final long glLightModelfv;
    public final long glLighti;
    public final long glLightf;
    public final long glLightiv;
    public final long glLightfv;
    public final long glLineStipple;
    public final long glLineWidth;
    public final long glListBase;
    public final long glLoadMatrixf;
    public final long glLoadMatrixd;
    public final long glLoadIdentity;
    public final long glLoadName;
    public final long glLogicOp;
    public final long glMap1f;
    public final long glMap1d;
    public final long glMap2f;
    public final long glMap2d;
    public final long glMapGrid1f;
    public final long glMapGrid1d;
    public final long glMapGrid2f;
    public final long glMapGrid2d;
    public final long glMateriali;
    public final long glMaterialf;
    public final long glMaterialiv;
    public final long glMaterialfv;
    public final long glMatrixMode;
    public final long glMultMatrixf;
    public final long glMultMatrixd;
    public final long glFrustum;
    public final long glNewList;
    public final long glEndList;
    public final long glNormal3f;
    public final long glNormal3b;
    public final long glNormal3s;
    public final long glNormal3i;
    public final long glNormal3d;
    public final long glNormal3fv;
    public final long glNormal3bv;
    public final long glNormal3sv;
    public final long glNormal3iv;
    public final long glNormal3dv;
    public final long glNormalPointer;
    public final long glOrtho;
    public final long glPassThrough;
    public final long glPixelMapfv;
    public final long glPixelMapusv;
    public final long glPixelMapuiv;
    public final long glPixelStorei;
    public final long glPixelStoref;
    public final long glPixelTransferi;
    public final long glPixelTransferf;
    public final long glPixelZoom;
    public final long glPointSize;
    public final long glPolygonMode;
    public final long glPolygonOffset;
    public final long glPolygonStipple;
    public final long glPushAttrib;
    public final long glPushClientAttrib;
    public final long glPopAttrib;
    public final long glPopClientAttrib;
    public final long glPopMatrix;
    public final long glPopName;
    public final long glPrioritizeTextures;
    public final long glPushMatrix;
    public final long glPushName;
    public final long glRasterPos2i;
    public final long glRasterPos2s;
    public final long glRasterPos2f;
    public final long glRasterPos2d;
    public final long glRasterPos2iv;
    public final long glRasterPos2sv;
    public final long glRasterPos2fv;
    public final long glRasterPos2dv;
    public final long glRasterPos3i;
    public final long glRasterPos3s;
    public final long glRasterPos3f;
    public final long glRasterPos3d;
    public final long glRasterPos3iv;
    public final long glRasterPos3sv;
    public final long glRasterPos3fv;
    public final long glRasterPos3dv;
    public final long glRasterPos4i;
    public final long glRasterPos4s;
    public final long glRasterPos4f;
    public final long glRasterPos4d;
    public final long glRasterPos4iv;
    public final long glRasterPos4sv;
    public final long glRasterPos4fv;
    public final long glRasterPos4dv;
    public final long glReadBuffer;
    public final long glReadPixels;
    public final long glRecti;
    public final long glRects;
    public final long glRectf;
    public final long glRectd;
    public final long glRectiv;
    public final long glRectsv;
    public final long glRectfv;
    public final long glRectdv;
    public final long glRenderMode;
    public final long glRotatef;
    public final long glRotated;
    public final long glScalef;
    public final long glScaled;
    public final long glScissor;
    public final long glSelectBuffer;
    public final long glShadeModel;
    public final long glStencilFunc;
    public final long glStencilMask;
    public final long glStencilOp;
    public final long glTexCoord1f;
    public final long glTexCoord1s;
    public final long glTexCoord1i;
    public final long glTexCoord1d;
    public final long glTexCoord1fv;
    public final long glTexCoord1sv;
    public final long glTexCoord1iv;
    public final long glTexCoord1dv;
    public final long glTexCoord2f;
    public final long glTexCoord2s;
    public final long glTexCoord2i;
    public final long glTexCoord2d;
    public final long glTexCoord2fv;
    public final long glTexCoord2sv;
    public final long glTexCoord2iv;
    public final long glTexCoord2dv;
    public final long glTexCoord3f;
    public final long glTexCoord3s;
    public final long glTexCoord3i;
    public final long glTexCoord3d;
    public final long glTexCoord3fv;
    public final long glTexCoord3sv;
    public final long glTexCoord3iv;
    public final long glTexCoord3dv;
    public final long glTexCoord4f;
    public final long glTexCoord4s;
    public final long glTexCoord4i;
    public final long glTexCoord4d;
    public final long glTexCoord4fv;
    public final long glTexCoord4sv;
    public final long glTexCoord4iv;
    public final long glTexCoord4dv;
    public final long glTexCoordPointer;
    public final long glTexEnvi;
    public final long glTexEnviv;
    public final long glTexEnvf;
    public final long glTexEnvfv;
    public final long glTexGeni;
    public final long glTexGeniv;
    public final long glTexGenf;
    public final long glTexGenfv;
    public final long glTexGend;
    public final long glTexGendv;
    public final long glTexImage1D;
    public final long glTexImage2D;
    public final long glCopyTexImage1D;
    public final long glCopyTexImage2D;
    public final long glCopyTexSubImage1D;
    public final long glCopyTexSubImage2D;
    public final long glTexParameteri;
    public final long glTexParameteriv;
    public final long glTexParameterf;
    public final long glTexParameterfv;
    public final long glTexSubImage1D;
    public final long glTexSubImage2D;
    public final long glTranslatef;
    public final long glTranslated;
    public final long glVertex2f;
    public final long glVertex2s;
    public final long glVertex2i;
    public final long glVertex2d;
    public final long glVertex2fv;
    public final long glVertex2sv;
    public final long glVertex2iv;
    public final long glVertex2dv;
    public final long glVertex3f;
    public final long glVertex3s;
    public final long glVertex3i;
    public final long glVertex3d;
    public final long glVertex3fv;
    public final long glVertex3sv;
    public final long glVertex3iv;
    public final long glVertex3dv;
    public final long glVertex4f;
    public final long glVertex4s;
    public final long glVertex4i;
    public final long glVertex4d;
    public final long glVertex4fv;
    public final long glVertex4sv;
    public final long glVertex4iv;
    public final long glVertex4dv;
    public final long glVertexPointer;
    public final long glViewport;
    public final long glTexImage3D;
    public final long glTexSubImage3D;
    public final long glCopyTexSubImage3D;
    public final long glDrawRangeElements;
    public final long glCompressedTexImage3D;
    public final long glCompressedTexImage2D;
    public final long glCompressedTexImage1D;
    public final long glCompressedTexSubImage3D;
    public final long glCompressedTexSubImage2D;
    public final long glCompressedTexSubImage1D;
    public final long glGetCompressedTexImage;
    public final long glSampleCoverage;
    public final long glActiveTexture;
    public final long glClientActiveTexture;
    public final long glMultiTexCoord1f;
    public final long glMultiTexCoord1s;
    public final long glMultiTexCoord1i;
    public final long glMultiTexCoord1d;
    public final long glMultiTexCoord1fv;
    public final long glMultiTexCoord1sv;
    public final long glMultiTexCoord1iv;
    public final long glMultiTexCoord1dv;
    public final long glMultiTexCoord2f;
    public final long glMultiTexCoord2s;
    public final long glMultiTexCoord2i;
    public final long glMultiTexCoord2d;
    public final long glMultiTexCoord2fv;
    public final long glMultiTexCoord2sv;
    public final long glMultiTexCoord2iv;
    public final long glMultiTexCoord2dv;
    public final long glMultiTexCoord3f;
    public final long glMultiTexCoord3s;
    public final long glMultiTexCoord3i;
    public final long glMultiTexCoord3d;
    public final long glMultiTexCoord3fv;
    public final long glMultiTexCoord3sv;
    public final long glMultiTexCoord3iv;
    public final long glMultiTexCoord3dv;
    public final long glMultiTexCoord4f;
    public final long glMultiTexCoord4s;
    public final long glMultiTexCoord4i;
    public final long glMultiTexCoord4d;
    public final long glMultiTexCoord4fv;
    public final long glMultiTexCoord4sv;
    public final long glMultiTexCoord4iv;
    public final long glMultiTexCoord4dv;
    public final long glLoadTransposeMatrixf;
    public final long glLoadTransposeMatrixd;
    public final long glMultTransposeMatrixf;
    public final long glMultTransposeMatrixd;
    public final long glBlendColor;
    public final long glBlendEquation;
    public final long glFogCoordf;
    public final long glFogCoordd;
    public final long glFogCoordfv;
    public final long glFogCoorddv;
    public final long glFogCoordPointer;
    public final long glMultiDrawArrays;
    public final long glMultiDrawElements;
    public final long glPointParameterf;
    public final long glPointParameteri;
    public final long glPointParameterfv;
    public final long glPointParameteriv;
    public final long glSecondaryColor3b;
    public final long glSecondaryColor3s;
    public final long glSecondaryColor3i;
    public final long glSecondaryColor3f;
    public final long glSecondaryColor3d;
    public final long glSecondaryColor3ub;
    public final long glSecondaryColor3us;
    public final long glSecondaryColor3ui;
    public final long glSecondaryColor3bv;
    public final long glSecondaryColor3sv;
    public final long glSecondaryColor3iv;
    public final long glSecondaryColor3fv;
    public final long glSecondaryColor3dv;
    public final long glSecondaryColor3ubv;
    public final long glSecondaryColor3usv;
    public final long glSecondaryColor3uiv;
    public final long glSecondaryColorPointer;
    public final long glBlendFuncSeparate;
    public final long glWindowPos2i;
    public final long glWindowPos2s;
    public final long glWindowPos2f;
    public final long glWindowPos2d;
    public final long glWindowPos2iv;
    public final long glWindowPos2sv;
    public final long glWindowPos2fv;
    public final long glWindowPos2dv;
    public final long glWindowPos3i;
    public final long glWindowPos3s;
    public final long glWindowPos3f;
    public final long glWindowPos3d;
    public final long glWindowPos3iv;
    public final long glWindowPos3sv;
    public final long glWindowPos3fv;
    public final long glWindowPos3dv;
    public final long glBindBuffer;
    public final long glDeleteBuffers;
    public final long glGenBuffers;
    public final long glIsBuffer;
    public final long glBufferData;
    public final long glBufferSubData;
    public final long glGetBufferSubData;
    public final long glMapBuffer;
    public final long glUnmapBuffer;
    public final long glGetBufferParameteriv;
    public final long glGetBufferPointerv;
    public final long glGenQueries;
    public final long glDeleteQueries;
    public final long glIsQuery;
    public final long glBeginQuery;
    public final long glEndQuery;
    public final long glGetQueryiv;
    public final long glGetQueryObjectiv;
    public final long glGetQueryObjectuiv;
    public final long glCreateProgram;
    public final long glDeleteProgram;
    public final long glIsProgram;
    public final long glCreateShader;
    public final long glDeleteShader;
    public final long glIsShader;
    public final long glAttachShader;
    public final long glDetachShader;
    public final long glShaderSource;
    public final long glCompileShader;
    public final long glLinkProgram;
    public final long glUseProgram;
    public final long glValidateProgram;
    public final long glUniform1f;
    public final long glUniform2f;
    public final long glUniform3f;
    public final long glUniform4f;
    public final long glUniform1i;
    public final long glUniform2i;
    public final long glUniform3i;
    public final long glUniform4i;
    public final long glUniform1fv;
    public final long glUniform2fv;
    public final long glUniform3fv;
    public final long glUniform4fv;
    public final long glUniform1iv;
    public final long glUniform2iv;
    public final long glUniform3iv;
    public final long glUniform4iv;
    public final long glUniformMatrix2fv;
    public final long glUniformMatrix3fv;
    public final long glUniformMatrix4fv;
    public final long glGetShaderiv;
    public final long glGetProgramiv;
    public final long glGetShaderInfoLog;
    public final long glGetProgramInfoLog;
    public final long glGetAttachedShaders;
    public final long glGetUniformLocation;
    public final long glGetActiveUniform;
    public final long glGetUniformfv;
    public final long glGetUniformiv;
    public final long glGetShaderSource;
    public final long glVertexAttrib1f;
    public final long glVertexAttrib1s;
    public final long glVertexAttrib1d;
    public final long glVertexAttrib2f;
    public final long glVertexAttrib2s;
    public final long glVertexAttrib2d;
    public final long glVertexAttrib3f;
    public final long glVertexAttrib3s;
    public final long glVertexAttrib3d;
    public final long glVertexAttrib4f;
    public final long glVertexAttrib4s;
    public final long glVertexAttrib4d;
    public final long glVertexAttrib4Nub;
    public final long glVertexAttrib1fv;
    public final long glVertexAttrib1sv;
    public final long glVertexAttrib1dv;
    public final long glVertexAttrib2fv;
    public final long glVertexAttrib2sv;
    public final long glVertexAttrib2dv;
    public final long glVertexAttrib3fv;
    public final long glVertexAttrib3sv;
    public final long glVertexAttrib3dv;
    public final long glVertexAttrib4fv;
    public final long glVertexAttrib4sv;
    public final long glVertexAttrib4dv;
    public final long glVertexAttrib4iv;
    public final long glVertexAttrib4bv;
    public final long glVertexAttrib4ubv;
    public final long glVertexAttrib4usv;
    public final long glVertexAttrib4uiv;
    public final long glVertexAttrib4Nbv;
    public final long glVertexAttrib4Nsv;
    public final long glVertexAttrib4Niv;
    public final long glVertexAttrib4Nubv;
    public final long glVertexAttrib4Nusv;
    public final long glVertexAttrib4Nuiv;
    public final long glVertexAttribPointer;
    public final long glEnableVertexAttribArray;
    public final long glDisableVertexAttribArray;
    public final long glBindAttribLocation;
    public final long glGetActiveAttrib;
    public final long glGetAttribLocation;
    public final long glGetVertexAttribiv;
    public final long glGetVertexAttribfv;
    public final long glGetVertexAttribdv;
    public final long glGetVertexAttribPointerv;
    public final long glDrawBuffers;
    public final long glBlendEquationSeparate;
    public final long glStencilOpSeparate;
    public final long glStencilFuncSeparate;
    public final long glStencilMaskSeparate;
    public final long glUniformMatrix2x3fv;
    public final long glUniformMatrix3x2fv;
    public final long glUniformMatrix2x4fv;
    public final long glUniformMatrix4x2fv;
    public final long glUniformMatrix3x4fv;
    public final long glUniformMatrix4x3fv;
    public final long glGetStringi;
    public final long glClearBufferiv;
    public final long glClearBufferuiv;
    public final long glClearBufferfv;
    public final long glClearBufferfi;
    public final long glVertexAttribI1i;
    public final long glVertexAttribI2i;
    public final long glVertexAttribI3i;
    public final long glVertexAttribI4i;
    public final long glVertexAttribI1ui;
    public final long glVertexAttribI2ui;
    public final long glVertexAttribI3ui;
    public final long glVertexAttribI4ui;
    public final long glVertexAttribI1iv;
    public final long glVertexAttribI2iv;
    public final long glVertexAttribI3iv;
    public final long glVertexAttribI4iv;
    public final long glVertexAttribI1uiv;
    public final long glVertexAttribI2uiv;
    public final long glVertexAttribI3uiv;
    public final long glVertexAttribI4uiv;
    public final long glVertexAttribI4bv;
    public final long glVertexAttribI4sv;
    public final long glVertexAttribI4ubv;
    public final long glVertexAttribI4usv;
    public final long glVertexAttribIPointer;
    public final long glGetVertexAttribIiv;
    public final long glGetVertexAttribIuiv;
    public final long glUniform1ui;
    public final long glUniform2ui;
    public final long glUniform3ui;
    public final long glUniform4ui;
    public final long glUniform1uiv;
    public final long glUniform2uiv;
    public final long glUniform3uiv;
    public final long glUniform4uiv;
    public final long glGetUniformuiv;
    public final long glBindFragDataLocation;
    public final long glGetFragDataLocation;
    public final long glBeginConditionalRender;
    public final long glEndConditionalRender;
    public final long glMapBufferRange;
    public final long glFlushMappedBufferRange;
    public final long glClampColor;
    public final long glIsRenderbuffer;
    public final long glBindRenderbuffer;
    public final long glDeleteRenderbuffers;
    public final long glGenRenderbuffers;
    public final long glRenderbufferStorage;
    public final long glRenderbufferStorageMultisample;
    public final long glGetRenderbufferParameteriv;
    public final long glIsFramebuffer;
    public final long glBindFramebuffer;
    public final long glDeleteFramebuffers;
    public final long glGenFramebuffers;
    public final long glCheckFramebufferStatus;
    public final long glFramebufferTexture1D;
    public final long glFramebufferTexture2D;
    public final long glFramebufferTexture3D;
    public final long glFramebufferTextureLayer;
    public final long glFramebufferRenderbuffer;
    public final long glGetFramebufferAttachmentParameteriv;
    public final long glBlitFramebuffer;
    public final long glGenerateMipmap;
    public final long glTexParameterIiv;
    public final long glTexParameterIuiv;
    public final long glGetTexParameterIiv;
    public final long glGetTexParameterIuiv;
    public final long glColorMaski;
    public final long glGetBooleani_v;
    public final long glGetIntegeri_v;
    public final long glEnablei;
    public final long glDisablei;
    public final long glIsEnabledi;
    public final long glBindBufferRange;
    public final long glBindBufferBase;
    public final long glBeginTransformFeedback;
    public final long glEndTransformFeedback;
    public final long glTransformFeedbackVaryings;
    public final long glGetTransformFeedbackVarying;
    public final long glBindVertexArray;
    public final long glDeleteVertexArrays;
    public final long glGenVertexArrays;
    public final long glIsVertexArray;
    public final long glDrawArraysInstanced;
    public final long glDrawElementsInstanced;
    public final long glCopyBufferSubData;
    public final long glPrimitiveRestartIndex;
    public final long glTexBuffer;
    public final long glGetUniformIndices;
    public final long glGetActiveUniformsiv;
    public final long glGetActiveUniformName;
    public final long glGetUniformBlockIndex;
    public final long glGetActiveUniformBlockiv;
    public final long glGetActiveUniformBlockName;
    public final long glUniformBlockBinding;
    public final long glGetBufferParameteri64v;
    public final long glDrawElementsBaseVertex;
    public final long glDrawRangeElementsBaseVertex;
    public final long glDrawElementsInstancedBaseVertex;
    public final long glMultiDrawElementsBaseVertex;
    public final long glProvokingVertex;
    public final long glTexImage2DMultisample;
    public final long glTexImage3DMultisample;
    public final long glGetMultisamplefv;
    public final long glSampleMaski;
    public final long glFramebufferTexture;
    public final long glFenceSync;
    public final long glIsSync;
    public final long glDeleteSync;
    public final long glClientWaitSync;
    public final long glWaitSync;
    public final long glGetInteger64v;
    public final long glGetInteger64i_v;
    public final long glGetSynciv;
    public final long glBindFragDataLocationIndexed;
    public final long glGetFragDataIndex;
    public final long glGenSamplers;
    public final long glDeleteSamplers;
    public final long glIsSampler;
    public final long glBindSampler;
    public final long glSamplerParameteri;
    public final long glSamplerParameterf;
    public final long glSamplerParameteriv;
    public final long glSamplerParameterfv;
    public final long glSamplerParameterIiv;
    public final long glSamplerParameterIuiv;
    public final long glGetSamplerParameteriv;
    public final long glGetSamplerParameterfv;
    public final long glGetSamplerParameterIiv;
    public final long glGetSamplerParameterIuiv;
    public final long glQueryCounter;
    public final long glGetQueryObjecti64v;
    public final long glGetQueryObjectui64v;
    public final long glVertexAttribDivisor;
    public final long glVertexP2ui;
    public final long glVertexP3ui;
    public final long glVertexP4ui;
    public final long glVertexP2uiv;
    public final long glVertexP3uiv;
    public final long glVertexP4uiv;
    public final long glTexCoordP1ui;
    public final long glTexCoordP2ui;
    public final long glTexCoordP3ui;
    public final long glTexCoordP4ui;
    public final long glTexCoordP1uiv;
    public final long glTexCoordP2uiv;
    public final long glTexCoordP3uiv;
    public final long glTexCoordP4uiv;
    public final long glMultiTexCoordP1ui;
    public final long glMultiTexCoordP2ui;
    public final long glMultiTexCoordP3ui;
    public final long glMultiTexCoordP4ui;
    public final long glMultiTexCoordP1uiv;
    public final long glMultiTexCoordP2uiv;
    public final long glMultiTexCoordP3uiv;
    public final long glMultiTexCoordP4uiv;
    public final long glNormalP3ui;
    public final long glNormalP3uiv;
    public final long glColorP3ui;
    public final long glColorP4ui;
    public final long glColorP3uiv;
    public final long glColorP4uiv;
    public final long glSecondaryColorP3ui;
    public final long glSecondaryColorP3uiv;
    public final long glVertexAttribP1ui;
    public final long glVertexAttribP2ui;
    public final long glVertexAttribP3ui;
    public final long glVertexAttribP4ui;
    public final long glVertexAttribP1uiv;
    public final long glVertexAttribP2uiv;
    public final long glVertexAttribP3uiv;
    public final long glVertexAttribP4uiv;
    public final long glBlendEquationi;
    public final long glBlendEquationSeparatei;
    public final long glBlendFunci;
    public final long glBlendFuncSeparatei;
    public final long glDrawArraysIndirect;
    public final long glDrawElementsIndirect;
    public final long glUniform1d;
    public final long glUniform2d;
    public final long glUniform3d;
    public final long glUniform4d;
    public final long glUniform1dv;
    public final long glUniform2dv;
    public final long glUniform3dv;
    public final long glUniform4dv;
    public final long glUniformMatrix2dv;
    public final long glUniformMatrix3dv;
    public final long glUniformMatrix4dv;
    public final long glUniformMatrix2x3dv;
    public final long glUniformMatrix2x4dv;
    public final long glUniformMatrix3x2dv;
    public final long glUniformMatrix3x4dv;
    public final long glUniformMatrix4x2dv;
    public final long glUniformMatrix4x3dv;
    public final long glGetUniformdv;
    public final long glMinSampleShading;
    public final long glGetSubroutineUniformLocation;
    public final long glGetSubroutineIndex;
    public final long glGetActiveSubroutineUniformiv;
    public final long glGetActiveSubroutineUniformName;
    public final long glGetActiveSubroutineName;
    public final long glUniformSubroutinesuiv;
    public final long glGetUniformSubroutineuiv;
    public final long glGetProgramStageiv;
    public final long glPatchParameteri;
    public final long glPatchParameterfv;
    public final long glBindTransformFeedback;
    public final long glDeleteTransformFeedbacks;
    public final long glGenTransformFeedbacks;
    public final long glIsTransformFeedback;
    public final long glPauseTransformFeedback;
    public final long glResumeTransformFeedback;
    public final long glDrawTransformFeedback;
    public final long glDrawTransformFeedbackStream;
    public final long glBeginQueryIndexed;
    public final long glEndQueryIndexed;
    public final long glGetQueryIndexediv;
    public final long glReleaseShaderCompiler;
    public final long glShaderBinary;
    public final long glGetShaderPrecisionFormat;
    public final long glDepthRangef;
    public final long glClearDepthf;
    public final long glGetProgramBinary;
    public final long glProgramBinary;
    public final long glProgramParameteri;
    public final long glUseProgramStages;
    public final long glActiveShaderProgram;
    public final long glCreateShaderProgramv;
    public final long glBindProgramPipeline;
    public final long glDeleteProgramPipelines;
    public final long glGenProgramPipelines;
    public final long glIsProgramPipeline;
    public final long glGetProgramPipelineiv;
    public final long glProgramUniform1i;
    public final long glProgramUniform2i;
    public final long glProgramUniform3i;
    public final long glProgramUniform4i;
    public final long glProgramUniform1ui;
    public final long glProgramUniform2ui;
    public final long glProgramUniform3ui;
    public final long glProgramUniform4ui;
    public final long glProgramUniform1f;
    public final long glProgramUniform2f;
    public final long glProgramUniform3f;
    public final long glProgramUniform4f;
    public final long glProgramUniform1d;
    public final long glProgramUniform2d;
    public final long glProgramUniform3d;
    public final long glProgramUniform4d;
    public final long glProgramUniform1iv;
    public final long glProgramUniform2iv;
    public final long glProgramUniform3iv;
    public final long glProgramUniform4iv;
    public final long glProgramUniform1uiv;
    public final long glProgramUniform2uiv;
    public final long glProgramUniform3uiv;
    public final long glProgramUniform4uiv;
    public final long glProgramUniform1fv;
    public final long glProgramUniform2fv;
    public final long glProgramUniform3fv;
    public final long glProgramUniform4fv;
    public final long glProgramUniform1dv;
    public final long glProgramUniform2dv;
    public final long glProgramUniform3dv;
    public final long glProgramUniform4dv;
    public final long glProgramUniformMatrix2fv;
    public final long glProgramUniformMatrix3fv;
    public final long glProgramUniformMatrix4fv;
    public final long glProgramUniformMatrix2dv;
    public final long glProgramUniformMatrix3dv;
    public final long glProgramUniformMatrix4dv;
    public final long glProgramUniformMatrix2x3fv;
    public final long glProgramUniformMatrix3x2fv;
    public final long glProgramUniformMatrix2x4fv;
    public final long glProgramUniformMatrix4x2fv;
    public final long glProgramUniformMatrix3x4fv;
    public final long glProgramUniformMatrix4x3fv;
    public final long glProgramUniformMatrix2x3dv;
    public final long glProgramUniformMatrix3x2dv;
    public final long glProgramUniformMatrix2x4dv;
    public final long glProgramUniformMatrix4x2dv;
    public final long glProgramUniformMatrix3x4dv;
    public final long glProgramUniformMatrix4x3dv;
    public final long glValidateProgramPipeline;
    public final long glGetProgramPipelineInfoLog;
    public final long glVertexAttribL1d;
    public final long glVertexAttribL2d;
    public final long glVertexAttribL3d;
    public final long glVertexAttribL4d;
    public final long glVertexAttribL1dv;
    public final long glVertexAttribL2dv;
    public final long glVertexAttribL3dv;
    public final long glVertexAttribL4dv;
    public final long glVertexAttribLPointer;
    public final long glGetVertexAttribLdv;
    public final long glViewportArrayv;
    public final long glViewportIndexedf;
    public final long glViewportIndexedfv;
    public final long glScissorArrayv;
    public final long glScissorIndexed;
    public final long glScissorIndexedv;
    public final long glDepthRangeArrayv;
    public final long glDepthRangeIndexed;
    public final long glGetFloati_v;
    public final long glGetDoublei_v;
    public final long glGetActiveAtomicCounterBufferiv;
    public final long glTexStorage1D;
    public final long glTexStorage2D;
    public final long glTexStorage3D;
    public final long glDrawTransformFeedbackInstanced;
    public final long glDrawTransformFeedbackStreamInstanced;
    public final long glDrawArraysInstancedBaseInstance;
    public final long glDrawElementsInstancedBaseInstance;
    public final long glDrawElementsInstancedBaseVertexBaseInstance;
    public final long glBindImageTexture;
    public final long glMemoryBarrier;
    public final long glGetInternalformativ;
    public final long glClearBufferData;
    public final long glClearBufferSubData;
    public final long glDispatchCompute;
    public final long glDispatchComputeIndirect;
    public final long glCopyImageSubData;
    public final long glDebugMessageControl;
    public final long glDebugMessageInsert;
    public final long glDebugMessageCallback;
    public final long glGetDebugMessageLog;
    public final long glPushDebugGroup;
    public final long glPopDebugGroup;
    public final long glObjectLabel;
    public final long glGetObjectLabel;
    public final long glObjectPtrLabel;
    public final long glGetObjectPtrLabel;
    public final long glFramebufferParameteri;
    public final long glGetFramebufferParameteriv;
    public final long glGetInternalformati64v;
    public final long glInvalidateTexSubImage;
    public final long glInvalidateTexImage;
    public final long glInvalidateBufferSubData;
    public final long glInvalidateBufferData;
    public final long glInvalidateFramebuffer;
    public final long glInvalidateSubFramebuffer;
    public final long glMultiDrawArraysIndirect;
    public final long glMultiDrawElementsIndirect;
    public final long glGetProgramInterfaceiv;
    public final long glGetProgramResourceIndex;
    public final long glGetProgramResourceName;
    public final long glGetProgramResourceiv;
    public final long glGetProgramResourceLocation;
    public final long glGetProgramResourceLocationIndex;
    public final long glShaderStorageBlockBinding;
    public final long glTexBufferRange;
    public final long glTexStorage2DMultisample;
    public final long glTexStorage3DMultisample;
    public final long glTextureView;
    public final long glBindVertexBuffer;
    public final long glVertexAttribFormat;
    public final long glVertexAttribIFormat;
    public final long glVertexAttribLFormat;
    public final long glVertexAttribBinding;
    public final long glVertexBindingDivisor;
    public final long glBufferStorage;
    public final long glClearTexSubImage;
    public final long glClearTexImage;
    public final long glBindBuffersBase;
    public final long glBindBuffersRange;
    public final long glBindTextures;
    public final long glBindSamplers;
    public final long glBindImageTextures;
    public final long glBindVertexBuffers;
    public final long glClipControl;
    public final long glCreateTransformFeedbacks;
    public final long glTransformFeedbackBufferBase;
    public final long glTransformFeedbackBufferRange;
    public final long glGetTransformFeedbackiv;
    public final long glGetTransformFeedbacki_v;
    public final long glGetTransformFeedbacki64_v;
    public final long glCreateBuffers;
    public final long glNamedBufferStorage;
    public final long glNamedBufferData;
    public final long glNamedBufferSubData;
    public final long glCopyNamedBufferSubData;
    public final long glClearNamedBufferData;
    public final long glClearNamedBufferSubData;
    public final long glMapNamedBuffer;
    public final long glMapNamedBufferRange;
    public final long glUnmapNamedBuffer;
    public final long glFlushMappedNamedBufferRange;
    public final long glGetNamedBufferParameteriv;
    public final long glGetNamedBufferParameteri64v;
    public final long glGetNamedBufferPointerv;
    public final long glGetNamedBufferSubData;
    public final long glCreateFramebuffers;
    public final long glNamedFramebufferRenderbuffer;
    public final long glNamedFramebufferParameteri;
    public final long glNamedFramebufferTexture;
    public final long glNamedFramebufferTextureLayer;
    public final long glNamedFramebufferDrawBuffer;
    public final long glNamedFramebufferDrawBuffers;
    public final long glNamedFramebufferReadBuffer;
    public final long glInvalidateNamedFramebufferData;
    public final long glInvalidateNamedFramebufferSubData;
    public final long glClearNamedFramebufferiv;
    public final long glClearNamedFramebufferuiv;
    public final long glClearNamedFramebufferfv;
    public final long glClearNamedFramebufferfi;
    public final long glBlitNamedFramebuffer;
    public final long glCheckNamedFramebufferStatus;
    public final long glGetNamedFramebufferParameteriv;
    public final long glGetNamedFramebufferAttachmentParameteriv;
    public final long glCreateRenderbuffers;
    public final long glNamedRenderbufferStorage;
    public final long glNamedRenderbufferStorageMultisample;
    public final long glGetNamedRenderbufferParameteriv;
    public final long glCreateTextures;
    public final long glTextureBuffer;
    public final long glTextureBufferRange;
    public final long glTextureStorage1D;
    public final long glTextureStorage2D;
    public final long glTextureStorage3D;
    public final long glTextureStorage2DMultisample;
    public final long glTextureStorage3DMultisample;
    public final long glTextureSubImage1D;
    public final long glTextureSubImage2D;
    public final long glTextureSubImage3D;
    public final long glCompressedTextureSubImage1D;
    public final long glCompressedTextureSubImage2D;
    public final long glCompressedTextureSubImage3D;
    public final long glCopyTextureSubImage1D;
    public final long glCopyTextureSubImage2D;
    public final long glCopyTextureSubImage3D;
    public final long glTextureParameterf;
    public final long glTextureParameterfv;
    public final long glTextureParameteri;
    public final long glTextureParameterIiv;
    public final long glTextureParameterIuiv;
    public final long glTextureParameteriv;
    public final long glGenerateTextureMipmap;
    public final long glBindTextureUnit;
    public final long glGetTextureImage;
    public final long glGetCompressedTextureImage;
    public final long glGetTextureLevelParameterfv;
    public final long glGetTextureLevelParameteriv;
    public final long glGetTextureParameterfv;
    public final long glGetTextureParameterIiv;
    public final long glGetTextureParameterIuiv;
    public final long glGetTextureParameteriv;
    public final long glCreateVertexArrays;
    public final long glDisableVertexArrayAttrib;
    public final long glEnableVertexArrayAttrib;
    public final long glVertexArrayElementBuffer;
    public final long glVertexArrayVertexBuffer;
    public final long glVertexArrayVertexBuffers;
    public final long glVertexArrayAttribFormat;
    public final long glVertexArrayAttribIFormat;
    public final long glVertexArrayAttribLFormat;
    public final long glVertexArrayAttribBinding;
    public final long glVertexArrayBindingDivisor;
    public final long glGetVertexArrayiv;
    public final long glGetVertexArrayIndexediv;
    public final long glGetVertexArrayIndexed64iv;
    public final long glCreateSamplers;
    public final long glCreateProgramPipelines;
    public final long glCreateQueries;
    public final long glGetQueryBufferObjectiv;
    public final long glGetQueryBufferObjectuiv;
    public final long glGetQueryBufferObjecti64v;
    public final long glGetQueryBufferObjectui64v;
    public final long glMemoryBarrierByRegion;
    public final long glGetTextureSubImage;
    public final long glGetCompressedTextureSubImage;
    public final long glTextureBarrier;
    public final long glGetGraphicsResetStatus;
    public final long glGetnMapdv;
    public final long glGetnMapfv;
    public final long glGetnMapiv;
    public final long glGetnPixelMapfv;
    public final long glGetnPixelMapuiv;
    public final long glGetnPixelMapusv;
    public final long glGetnPolygonStipple;
    public final long glGetnTexImage;
    public final long glReadnPixels;
    public final long glGetnColorTable;
    public final long glGetnConvolutionFilter;
    public final long glGetnSeparableFilter;
    public final long glGetnHistogram;
    public final long glGetnMinmax;
    public final long glGetnCompressedTexImage;
    public final long glGetnUniformfv;
    public final long glGetnUniformdv;
    public final long glGetnUniformiv;
    public final long glGetnUniformuiv;
    public final long glMultiDrawArraysIndirectCount;
    public final long glMultiDrawElementsIndirectCount;
    public final long glPolygonOffsetClamp;
    public final long glSpecializeShader;
    public final long glDebugMessageEnableAMD;
    public final long glDebugMessageInsertAMD;
    public final long glDebugMessageCallbackAMD;
    public final long glGetDebugMessageLogAMD;
    public final long glBlendFuncIndexedAMD;
    public final long glBlendFuncSeparateIndexedAMD;
    public final long glBlendEquationIndexedAMD;
    public final long glBlendEquationSeparateIndexedAMD;
    public final long glRenderbufferStorageMultisampleAdvancedAMD;
    public final long glNamedRenderbufferStorageMultisampleAdvancedAMD;
    public final long glUniform1i64NV;
    public final long glUniform2i64NV;
    public final long glUniform3i64NV;
    public final long glUniform4i64NV;
    public final long glUniform1i64vNV;
    public final long glUniform2i64vNV;
    public final long glUniform3i64vNV;
    public final long glUniform4i64vNV;
    public final long glUniform1ui64NV;
    public final long glUniform2ui64NV;
    public final long glUniform3ui64NV;
    public final long glUniform4ui64NV;
    public final long glUniform1ui64vNV;
    public final long glUniform2ui64vNV;
    public final long glUniform3ui64vNV;
    public final long glUniform4ui64vNV;
    public final long glGetUniformi64vNV;
    public final long glGetUniformui64vNV;
    public final long glProgramUniform1i64NV;
    public final long glProgramUniform2i64NV;
    public final long glProgramUniform3i64NV;
    public final long glProgramUniform4i64NV;
    public final long glProgramUniform1i64vNV;
    public final long glProgramUniform2i64vNV;
    public final long glProgramUniform3i64vNV;
    public final long glProgramUniform4i64vNV;
    public final long glProgramUniform1ui64NV;
    public final long glProgramUniform2ui64NV;
    public final long glProgramUniform3ui64NV;
    public final long glProgramUniform4ui64NV;
    public final long glProgramUniform1ui64vNV;
    public final long glProgramUniform2ui64vNV;
    public final long glProgramUniform3ui64vNV;
    public final long glProgramUniform4ui64vNV;
    public final long glVertexAttribParameteriAMD;
    public final long glQueryObjectParameteruiAMD;
    public final long glGetPerfMonitorGroupsAMD;
    public final long glGetPerfMonitorCountersAMD;
    public final long glGetPerfMonitorGroupStringAMD;
    public final long glGetPerfMonitorCounterStringAMD;
    public final long glGetPerfMonitorCounterInfoAMD;
    public final long glGenPerfMonitorsAMD;
    public final long glDeletePerfMonitorsAMD;
    public final long glSelectPerfMonitorCountersAMD;
    public final long glBeginPerfMonitorAMD;
    public final long glEndPerfMonitorAMD;
    public final long glGetPerfMonitorCounterDataAMD;
    public final long glSetMultisamplefvAMD;
    public final long glTexStorageSparseAMD;
    public final long glTextureStorageSparseAMD;
    public final long glStencilOpValueAMD;
    public final long glTessellationFactorAMD;
    public final long glTessellationModeAMD;
    public final long glGetTextureHandleARB;
    public final long glGetTextureSamplerHandleARB;
    public final long glMakeTextureHandleResidentARB;
    public final long glMakeTextureHandleNonResidentARB;
    public final long glGetImageHandleARB;
    public final long glMakeImageHandleResidentARB;
    public final long glMakeImageHandleNonResidentARB;
    public final long glUniformHandleui64ARB;
    public final long glUniformHandleui64vARB;
    public final long glProgramUniformHandleui64ARB;
    public final long glProgramUniformHandleui64vARB;
    public final long glIsTextureHandleResidentARB;
    public final long glIsImageHandleResidentARB;
    public final long glVertexAttribL1ui64ARB;
    public final long glVertexAttribL1ui64vARB;
    public final long glGetVertexAttribLui64vARB;
    public final long glNamedBufferStorageEXT;
    public final long glCreateSyncFromCLeventARB;
    public final long glClearNamedBufferDataEXT;
    public final long glClearNamedBufferSubDataEXT;
    public final long glClampColorARB;
    public final long glDispatchComputeGroupSizeARB;
    public final long glDebugMessageControlARB;
    public final long glDebugMessageInsertARB;
    public final long glDebugMessageCallbackARB;
    public final long glGetDebugMessageLogARB;
    public final long glDrawBuffersARB;
    public final long glBlendEquationiARB;
    public final long glBlendEquationSeparateiARB;
    public final long glBlendFunciARB;
    public final long glBlendFuncSeparateiARB;
    public final long glDrawArraysInstancedARB;
    public final long glDrawElementsInstancedARB;
    public final long glPrimitiveBoundingBoxARB;
    public final long glNamedFramebufferParameteriEXT;
    public final long glGetNamedFramebufferParameterivEXT;
    public final long glProgramParameteriARB;
    public final long glFramebufferTextureARB;
    public final long glFramebufferTextureLayerARB;
    public final long glFramebufferTextureFaceARB;
    public final long glSpecializeShaderARB;
    public final long glProgramUniform1dEXT;
    public final long glProgramUniform2dEXT;
    public final long glProgramUniform3dEXT;
    public final long glProgramUniform4dEXT;
    public final long glProgramUniform1dvEXT;
    public final long glProgramUniform2dvEXT;
    public final long glProgramUniform3dvEXT;
    public final long glProgramUniform4dvEXT;
    public final long glProgramUniformMatrix2dvEXT;
    public final long glProgramUniformMatrix3dvEXT;
    public final long glProgramUniformMatrix4dvEXT;
    public final long glProgramUniformMatrix2x3dvEXT;
    public final long glProgramUniformMatrix2x4dvEXT;
    public final long glProgramUniformMatrix3x2dvEXT;
    public final long glProgramUniformMatrix3x4dvEXT;
    public final long glProgramUniformMatrix4x2dvEXT;
    public final long glProgramUniformMatrix4x3dvEXT;
    public final long glUniform1i64ARB;
    public final long glUniform1i64vARB;
    public final long glProgramUniform1i64ARB;
    public final long glProgramUniform1i64vARB;
    public final long glUniform2i64ARB;
    public final long glUniform2i64vARB;
    public final long glProgramUniform2i64ARB;
    public final long glProgramUniform2i64vARB;
    public final long glUniform3i64ARB;
    public final long glUniform3i64vARB;
    public final long glProgramUniform3i64ARB;
    public final long glProgramUniform3i64vARB;
    public final long glUniform4i64ARB;
    public final long glUniform4i64vARB;
    public final long glProgramUniform4i64ARB;
    public final long glProgramUniform4i64vARB;
    public final long glUniform1ui64ARB;
    public final long glUniform1ui64vARB;
    public final long glProgramUniform1ui64ARB;
    public final long glProgramUniform1ui64vARB;
    public final long glUniform2ui64ARB;
    public final long glUniform2ui64vARB;
    public final long glProgramUniform2ui64ARB;
    public final long glProgramUniform2ui64vARB;
    public final long glUniform3ui64ARB;
    public final long glUniform3ui64vARB;
    public final long glProgramUniform3ui64ARB;
    public final long glProgramUniform3ui64vARB;
    public final long glUniform4ui64ARB;
    public final long glUniform4ui64vARB;
    public final long glProgramUniform4ui64ARB;
    public final long glProgramUniform4ui64vARB;
    public final long glGetUniformi64vARB;
    public final long glGetUniformui64vARB;
    public final long glGetnUniformi64vARB;
    public final long glGetnUniformui64vARB;
    public final long glColorTable;
    public final long glCopyColorTable;
    public final long glColorTableParameteriv;
    public final long glColorTableParameterfv;
    public final long glGetColorTable;
    public final long glGetColorTableParameteriv;
    public final long glGetColorTableParameterfv;
    public final long glColorSubTable;
    public final long glCopyColorSubTable;
    public final long glConvolutionFilter1D;
    public final long glConvolutionFilter2D;
    public final long glCopyConvolutionFilter1D;
    public final long glCopyConvolutionFilter2D;
    public final long glGetConvolutionFilter;
    public final long glSeparableFilter2D;
    public final long glGetSeparableFilter;
    public final long glConvolutionParameteri;
    public final long glConvolutionParameteriv;
    public final long glConvolutionParameterf;
    public final long glConvolutionParameterfv;
    public final long glGetConvolutionParameteriv;
    public final long glGetConvolutionParameterfv;
    public final long glHistogram;
    public final long glResetHistogram;
    public final long glGetHistogram;
    public final long glGetHistogramParameteriv;
    public final long glGetHistogramParameterfv;
    public final long glMinmax;
    public final long glResetMinmax;
    public final long glGetMinmax;
    public final long glGetMinmaxParameteriv;
    public final long glGetMinmaxParameterfv;
    public final long glMultiDrawArraysIndirectCountARB;
    public final long glMultiDrawElementsIndirectCountARB;
    public final long glVertexAttribDivisorARB;
    public final long glVertexArrayVertexAttribDivisorEXT;
    public final long glCurrentPaletteMatrixARB;
    public final long glMatrixIndexuivARB;
    public final long glMatrixIndexubvARB;
    public final long glMatrixIndexusvARB;
    public final long glMatrixIndexPointerARB;
    public final long glSampleCoverageARB;
    public final long glActiveTextureARB;
    public final long glClientActiveTextureARB;
    public final long glMultiTexCoord1fARB;
    public final long glMultiTexCoord1sARB;
    public final long glMultiTexCoord1iARB;
    public final long glMultiTexCoord1dARB;
    public final long glMultiTexCoord1fvARB;
    public final long glMultiTexCoord1svARB;
    public final long glMultiTexCoord1ivARB;
    public final long glMultiTexCoord1dvARB;
    public final long glMultiTexCoord2fARB;
    public final long glMultiTexCoord2sARB;
    public final long glMultiTexCoord2iARB;
    public final long glMultiTexCoord2dARB;
    public final long glMultiTexCoord2fvARB;
    public final long glMultiTexCoord2svARB;
    public final long glMultiTexCoord2ivARB;
    public final long glMultiTexCoord2dvARB;
    public final long glMultiTexCoord3fARB;
    public final long glMultiTexCoord3sARB;
    public final long glMultiTexCoord3iARB;
    public final long glMultiTexCoord3dARB;
    public final long glMultiTexCoord3fvARB;
    public final long glMultiTexCoord3svARB;
    public final long glMultiTexCoord3ivARB;
    public final long glMultiTexCoord3dvARB;
    public final long glMultiTexCoord4fARB;
    public final long glMultiTexCoord4sARB;
    public final long glMultiTexCoord4iARB;
    public final long glMultiTexCoord4dARB;
    public final long glMultiTexCoord4fvARB;
    public final long glMultiTexCoord4svARB;
    public final long glMultiTexCoord4ivARB;
    public final long glMultiTexCoord4dvARB;
    public final long glGenQueriesARB;
    public final long glDeleteQueriesARB;
    public final long glIsQueryARB;
    public final long glBeginQueryARB;
    public final long glEndQueryARB;
    public final long glGetQueryivARB;
    public final long glGetQueryObjectivARB;
    public final long glGetQueryObjectuivARB;
    public final long glMaxShaderCompilerThreadsARB;
    public final long glPointParameterfARB;
    public final long glPointParameterfvARB;
    public final long glGetGraphicsResetStatusARB;
    public final long glGetnMapdvARB;
    public final long glGetnMapfvARB;
    public final long glGetnMapivARB;
    public final long glGetnPixelMapfvARB;
    public final long glGetnPixelMapuivARB;
    public final long glGetnPixelMapusvARB;
    public final long glGetnPolygonStippleARB;
    public final long glGetnTexImageARB;
    public final long glReadnPixelsARB;
    public final long glGetnColorTableARB;
    public final long glGetnConvolutionFilterARB;
    public final long glGetnSeparableFilterARB;
    public final long glGetnHistogramARB;
    public final long glGetnMinmaxARB;
    public final long glGetnCompressedTexImageARB;
    public final long glGetnUniformfvARB;
    public final long glGetnUniformivARB;
    public final long glGetnUniformuivARB;
    public final long glGetnUniformdvARB;
    public final long glFramebufferSampleLocationsfvARB;
    public final long glNamedFramebufferSampleLocationsfvARB;
    public final long glEvaluateDepthValuesARB;
    public final long glMinSampleShadingARB;
    public final long glDeleteObjectARB;
    public final long glGetHandleARB;
    public final long glDetachObjectARB;
    public final long glCreateShaderObjectARB;
    public final long glShaderSourceARB;
    public final long glCompileShaderARB;
    public final long glCreateProgramObjectARB;
    public final long glAttachObjectARB;
    public final long glLinkProgramARB;
    public final long glUseProgramObjectARB;
    public final long glValidateProgramARB;
    public final long glUniform1fARB;
    public final long glUniform2fARB;
    public final long glUniform3fARB;
    public final long glUniform4fARB;
    public final long glUniform1iARB;
    public final long glUniform2iARB;
    public final long glUniform3iARB;
    public final long glUniform4iARB;
    public final long glUniform1fvARB;
    public final long glUniform2fvARB;
    public final long glUniform3fvARB;
    public final long glUniform4fvARB;
    public final long glUniform1ivARB;
    public final long glUniform2ivARB;
    public final long glUniform3ivARB;
    public final long glUniform4ivARB;
    public final long glUniformMatrix2fvARB;
    public final long glUniformMatrix3fvARB;
    public final long glUniformMatrix4fvARB;
    public final long glGetObjectParameterfvARB;
    public final long glGetObjectParameterivARB;
    public final long glGetInfoLogARB;
    public final long glGetAttachedObjectsARB;
    public final long glGetUniformLocationARB;
    public final long glGetActiveUniformARB;
    public final long glGetUniformfvARB;
    public final long glGetUniformivARB;
    public final long glGetShaderSourceARB;
    public final long glNamedStringARB;
    public final long glDeleteNamedStringARB;
    public final long glCompileShaderIncludeARB;
    public final long glIsNamedStringARB;
    public final long glGetNamedStringARB;
    public final long glGetNamedStringivARB;
    public final long glBufferPageCommitmentARB;
    public final long glNamedBufferPageCommitmentEXT;
    public final long glNamedBufferPageCommitmentARB;
    public final long glTexPageCommitmentARB;
    public final long glTexturePageCommitmentEXT;
    public final long glTexBufferARB;
    public final long glTextureBufferRangeEXT;
    public final long glCompressedTexImage3DARB;
    public final long glCompressedTexImage2DARB;
    public final long glCompressedTexImage1DARB;
    public final long glCompressedTexSubImage3DARB;
    public final long glCompressedTexSubImage2DARB;
    public final long glCompressedTexSubImage1DARB;
    public final long glGetCompressedTexImageARB;
    public final long glTextureStorage1DEXT;
    public final long glTextureStorage2DEXT;
    public final long glTextureStorage3DEXT;
    public final long glTextureStorage2DMultisampleEXT;
    public final long glTextureStorage3DMultisampleEXT;
    public final long glLoadTransposeMatrixfARB;
    public final long glLoadTransposeMatrixdARB;
    public final long glMultTransposeMatrixfARB;
    public final long glMultTransposeMatrixdARB;
    public final long glVertexArrayVertexAttribLOffsetEXT;
    public final long glVertexArrayBindVertexBufferEXT;
    public final long glVertexArrayVertexAttribFormatEXT;
    public final long glVertexArrayVertexAttribIFormatEXT;
    public final long glVertexArrayVertexAttribLFormatEXT;
    public final long glVertexArrayVertexAttribBindingEXT;
    public final long glVertexArrayVertexBindingDivisorEXT;
    public final long glWeightfvARB;
    public final long glWeightbvARB;
    public final long glWeightubvARB;
    public final long glWeightsvARB;
    public final long glWeightusvARB;
    public final long glWeightivARB;
    public final long glWeightuivARB;
    public final long glWeightdvARB;
    public final long glWeightPointerARB;
    public final long glVertexBlendARB;
    public final long glBindBufferARB;
    public final long glDeleteBuffersARB;
    public final long glGenBuffersARB;
    public final long glIsBufferARB;
    public final long glBufferDataARB;
    public final long glBufferSubDataARB;
    public final long glGetBufferSubDataARB;
    public final long glMapBufferARB;
    public final long glUnmapBufferARB;
    public final long glGetBufferParameterivARB;
    public final long glGetBufferPointervARB;
    public final long glVertexAttrib1sARB;
    public final long glVertexAttrib1fARB;
    public final long glVertexAttrib1dARB;
    public final long glVertexAttrib2sARB;
    public final long glVertexAttrib2fARB;
    public final long glVertexAttrib2dARB;
    public final long glVertexAttrib3sARB;
    public final long glVertexAttrib3fARB;
    public final long glVertexAttrib3dARB;
    public final long glVertexAttrib4sARB;
    public final long glVertexAttrib4fARB;
    public final long glVertexAttrib4dARB;
    public final long glVertexAttrib4NubARB;
    public final long glVertexAttrib1svARB;
    public final long glVertexAttrib1fvARB;
    public final long glVertexAttrib1dvARB;
    public final long glVertexAttrib2svARB;
    public final long glVertexAttrib2fvARB;
    public final long glVertexAttrib2dvARB;
    public final long glVertexAttrib3svARB;
    public final long glVertexAttrib3fvARB;
    public final long glVertexAttrib3dvARB;
    public final long glVertexAttrib4fvARB;
    public final long glVertexAttrib4bvARB;
    public final long glVertexAttrib4svARB;
    public final long glVertexAttrib4ivARB;
    public final long glVertexAttrib4ubvARB;
    public final long glVertexAttrib4usvARB;
    public final long glVertexAttrib4uivARB;
    public final long glVertexAttrib4dvARB;
    public final long glVertexAttrib4NbvARB;
    public final long glVertexAttrib4NsvARB;
    public final long glVertexAttrib4NivARB;
    public final long glVertexAttrib4NubvARB;
    public final long glVertexAttrib4NusvARB;
    public final long glVertexAttrib4NuivARB;
    public final long glVertexAttribPointerARB;
    public final long glEnableVertexAttribArrayARB;
    public final long glDisableVertexAttribArrayARB;
    public final long glProgramStringARB;
    public final long glBindProgramARB;
    public final long glDeleteProgramsARB;
    public final long glGenProgramsARB;
    public final long glProgramEnvParameter4dARB;
    public final long glProgramEnvParameter4dvARB;
    public final long glProgramEnvParameter4fARB;
    public final long glProgramEnvParameter4fvARB;
    public final long glProgramLocalParameter4dARB;
    public final long glProgramLocalParameter4dvARB;
    public final long glProgramLocalParameter4fARB;
    public final long glProgramLocalParameter4fvARB;
    public final long glGetProgramEnvParameterfvARB;
    public final long glGetProgramEnvParameterdvARB;
    public final long glGetProgramLocalParameterfvARB;
    public final long glGetProgramLocalParameterdvARB;
    public final long glGetProgramivARB;
    public final long glGetProgramStringARB;
    public final long glGetVertexAttribfvARB;
    public final long glGetVertexAttribdvARB;
    public final long glGetVertexAttribivARB;
    public final long glGetVertexAttribPointervARB;
    public final long glIsProgramARB;
    public final long glBindAttribLocationARB;
    public final long glGetActiveAttribARB;
    public final long glGetAttribLocationARB;
    public final long glWindowPos2iARB;
    public final long glWindowPos2sARB;
    public final long glWindowPos2fARB;
    public final long glWindowPos2dARB;
    public final long glWindowPos2ivARB;
    public final long glWindowPos2svARB;
    public final long glWindowPos2fvARB;
    public final long glWindowPos2dvARB;
    public final long glWindowPos3iARB;
    public final long glWindowPos3sARB;
    public final long glWindowPos3fARB;
    public final long glWindowPos3dARB;
    public final long glWindowPos3ivARB;
    public final long glWindowPos3svARB;
    public final long glWindowPos3fvARB;
    public final long glWindowPos3dvARB;
    public final long glUniformBufferEXT;
    public final long glGetUniformBufferSizeEXT;
    public final long glGetUniformOffsetEXT;
    public final long glBlendColorEXT;
    public final long glBlendEquationSeparateEXT;
    public final long glBlendFuncSeparateEXT;
    public final long glBlendEquationEXT;
    public final long glLockArraysEXT;
    public final long glUnlockArraysEXT;
    public final long glLabelObjectEXT;
    public final long glGetObjectLabelEXT;
    public final long glInsertEventMarkerEXT;
    public final long glPushGroupMarkerEXT;
    public final long glPopGroupMarkerEXT;
    public final long glDepthBoundsEXT;
    public final long glClientAttribDefaultEXT;
    public final long glPushClientAttribDefaultEXT;
    public final long glMatrixLoadfEXT;
    public final long glMatrixLoaddEXT;
    public final long glMatrixMultfEXT;
    public final long glMatrixMultdEXT;
    public final long glMatrixLoadIdentityEXT;
    public final long glMatrixRotatefEXT;
    public final long glMatrixRotatedEXT;
    public final long glMatrixScalefEXT;
    public final long glMatrixScaledEXT;
    public final long glMatrixTranslatefEXT;
    public final long glMatrixTranslatedEXT;
    public final long glMatrixOrthoEXT;
    public final long glMatrixFrustumEXT;
    public final long glMatrixPushEXT;
    public final long glMatrixPopEXT;
    public final long glTextureParameteriEXT;
    public final long glTextureParameterivEXT;
    public final long glTextureParameterfEXT;
    public final long glTextureParameterfvEXT;
    public final long glTextureImage1DEXT;
    public final long glTextureImage2DEXT;
    public final long glTextureSubImage1DEXT;
    public final long glTextureSubImage2DEXT;
    public final long glCopyTextureImage1DEXT;
    public final long glCopyTextureImage2DEXT;
    public final long glCopyTextureSubImage1DEXT;
    public final long glCopyTextureSubImage2DEXT;
    public final long glGetTextureImageEXT;
    public final long glGetTextureParameterfvEXT;
    public final long glGetTextureParameterivEXT;
    public final long glGetTextureLevelParameterfvEXT;
    public final long glGetTextureLevelParameterivEXT;
    public final long glTextureImage3DEXT;
    public final long glTextureSubImage3DEXT;
    public final long glCopyTextureSubImage3DEXT;
    public final long glBindMultiTextureEXT;
    public final long glMultiTexCoordPointerEXT;
    public final long glMultiTexEnvfEXT;
    public final long glMultiTexEnvfvEXT;
    public final long glMultiTexEnviEXT;
    public final long glMultiTexEnvivEXT;
    public final long glMultiTexGendEXT;
    public final long glMultiTexGendvEXT;
    public final long glMultiTexGenfEXT;
    public final long glMultiTexGenfvEXT;
    public final long glMultiTexGeniEXT;
    public final long glMultiTexGenivEXT;
    public final long glGetMultiTexEnvfvEXT;
    public final long glGetMultiTexEnvivEXT;
    public final long glGetMultiTexGendvEXT;
    public final long glGetMultiTexGenfvEXT;
    public final long glGetMultiTexGenivEXT;
    public final long glMultiTexParameteriEXT;
    public final long glMultiTexParameterivEXT;
    public final long glMultiTexParameterfEXT;
    public final long glMultiTexParameterfvEXT;
    public final long glMultiTexImage1DEXT;
    public final long glMultiTexImage2DEXT;
    public final long glMultiTexSubImage1DEXT;
    public final long glMultiTexSubImage2DEXT;
    public final long glCopyMultiTexImage1DEXT;
    public final long glCopyMultiTexImage2DEXT;
    public final long glCopyMultiTexSubImage1DEXT;
    public final long glCopyMultiTexSubImage2DEXT;
    public final long glGetMultiTexImageEXT;
    public final long glGetMultiTexParameterfvEXT;
    public final long glGetMultiTexParameterivEXT;
    public final long glGetMultiTexLevelParameterfvEXT;
    public final long glGetMultiTexLevelParameterivEXT;
    public final long glMultiTexImage3DEXT;
    public final long glMultiTexSubImage3DEXT;
    public final long glCopyMultiTexSubImage3DEXT;
    public final long glEnableClientStateIndexedEXT;
    public final long glDisableClientStateIndexedEXT;
    public final long glEnableClientStateiEXT;
    public final long glDisableClientStateiEXT;
    public final long glGetFloatIndexedvEXT;
    public final long glGetDoubleIndexedvEXT;
    public final long glGetPointerIndexedvEXT;
    public final long glGetFloati_vEXT;
    public final long glGetDoublei_vEXT;
    public final long glGetPointeri_vEXT;
    public final long glEnableIndexedEXT;
    public final long glDisableIndexedEXT;
    public final long glIsEnabledIndexedEXT;
    public final long glGetIntegerIndexedvEXT;
    public final long glGetBooleanIndexedvEXT;
    public final long glNamedProgramStringEXT;
    public final long glNamedProgramLocalParameter4dEXT;
    public final long glNamedProgramLocalParameter4dvEXT;
    public final long glNamedProgramLocalParameter4fEXT;
    public final long glNamedProgramLocalParameter4fvEXT;
    public final long glGetNamedProgramLocalParameterdvEXT;
    public final long glGetNamedProgramLocalParameterfvEXT;
    public final long glGetNamedProgramivEXT;
    public final long glGetNamedProgramStringEXT;
    public final long glCompressedTextureImage3DEXT;
    public final long glCompressedTextureImage2DEXT;
    public final long glCompressedTextureImage1DEXT;
    public final long glCompressedTextureSubImage3DEXT;
    public final long glCompressedTextureSubImage2DEXT;
    public final long glCompressedTextureSubImage1DEXT;
    public final long glGetCompressedTextureImageEXT;
    public final long glCompressedMultiTexImage3DEXT;
    public final long glCompressedMultiTexImage2DEXT;
    public final long glCompressedMultiTexImage1DEXT;
    public final long glCompressedMultiTexSubImage3DEXT;
    public final long glCompressedMultiTexSubImage2DEXT;
    public final long glCompressedMultiTexSubImage1DEXT;
    public final long glGetCompressedMultiTexImageEXT;
    public final long glMatrixLoadTransposefEXT;
    public final long glMatrixLoadTransposedEXT;
    public final long glMatrixMultTransposefEXT;
    public final long glMatrixMultTransposedEXT;
    public final long glNamedBufferDataEXT;
    public final long glNamedBufferSubDataEXT;
    public final long glMapNamedBufferEXT;
    public final long glUnmapNamedBufferEXT;
    public final long glGetNamedBufferParameterivEXT;
    public final long glGetNamedBufferSubDataEXT;
    public final long glProgramUniform1fEXT;
    public final long glProgramUniform2fEXT;
    public final long glProgramUniform3fEXT;
    public final long glProgramUniform4fEXT;
    public final long glProgramUniform1iEXT;
    public final long glProgramUniform2iEXT;
    public final long glProgramUniform3iEXT;
    public final long glProgramUniform4iEXT;
    public final long glProgramUniform1fvEXT;
    public final long glProgramUniform2fvEXT;
    public final long glProgramUniform3fvEXT;
    public final long glProgramUniform4fvEXT;
    public final long glProgramUniform1ivEXT;
    public final long glProgramUniform2ivEXT;
    public final long glProgramUniform3ivEXT;
    public final long glProgramUniform4ivEXT;
    public final long glProgramUniformMatrix2fvEXT;
    public final long glProgramUniformMatrix3fvEXT;
    public final long glProgramUniformMatrix4fvEXT;
    public final long glProgramUniformMatrix2x3fvEXT;
    public final long glProgramUniformMatrix3x2fvEXT;
    public final long glProgramUniformMatrix2x4fvEXT;
    public final long glProgramUniformMatrix4x2fvEXT;
    public final long glProgramUniformMatrix3x4fvEXT;
    public final long glProgramUniformMatrix4x3fvEXT;
    public final long glTextureBufferEXT;
    public final long glMultiTexBufferEXT;
    public final long glTextureParameterIivEXT;
    public final long glTextureParameterIuivEXT;
    public final long glGetTextureParameterIivEXT;
    public final long glGetTextureParameterIuivEXT;
    public final long glMultiTexParameterIivEXT;
    public final long glMultiTexParameterIuivEXT;
    public final long glGetMultiTexParameterIivEXT;
    public final long glGetMultiTexParameterIuivEXT;
    public final long glProgramUniform1uiEXT;
    public final long glProgramUniform2uiEXT;
    public final long glProgramUniform3uiEXT;
    public final long glProgramUniform4uiEXT;
    public final long glProgramUniform1uivEXT;
    public final long glProgramUniform2uivEXT;
    public final long glProgramUniform3uivEXT;
    public final long glProgramUniform4uivEXT;
    public final long glNamedProgramLocalParameters4fvEXT;
    public final long glNamedProgramLocalParameterI4iEXT;
    public final long glNamedProgramLocalParameterI4ivEXT;
    public final long glNamedProgramLocalParametersI4ivEXT;
    public final long glNamedProgramLocalParameterI4uiEXT;
    public final long glNamedProgramLocalParameterI4uivEXT;
    public final long glNamedProgramLocalParametersI4uivEXT;
    public final long glGetNamedProgramLocalParameterIivEXT;
    public final long glGetNamedProgramLocalParameterIuivEXT;
    public final long glNamedRenderbufferStorageEXT;
    public final long glGetNamedRenderbufferParameterivEXT;
    public final long glNamedRenderbufferStorageMultisampleEXT;
    public final long glNamedRenderbufferStorageMultisampleCoverageEXT;
    public final long glCheckNamedFramebufferStatusEXT;
    public final long glNamedFramebufferTexture1DEXT;
    public final long glNamedFramebufferTexture2DEXT;
    public final long glNamedFramebufferTexture3DEXT;
    public final long glNamedFramebufferRenderbufferEXT;
    public final long glGetNamedFramebufferAttachmentParameterivEXT;
    public final long glGenerateTextureMipmapEXT;
    public final long glGenerateMultiTexMipmapEXT;
    public final long glFramebufferDrawBufferEXT;
    public final long glFramebufferDrawBuffersEXT;
    public final long glFramebufferReadBufferEXT;
    public final long glGetFramebufferParameterivEXT;
    public final long glNamedCopyBufferSubDataEXT;
    public final long glNamedFramebufferTextureEXT;
    public final long glNamedFramebufferTextureLayerEXT;
    public final long glNamedFramebufferTextureFaceEXT;
    public final long glTextureRenderbufferEXT;
    public final long glMultiTexRenderbufferEXT;
    public final long glVertexArrayVertexOffsetEXT;
    public final long glVertexArrayColorOffsetEXT;
    public final long glVertexArrayEdgeFlagOffsetEXT;
    public final long glVertexArrayIndexOffsetEXT;
    public final long glVertexArrayNormalOffsetEXT;
    public final long glVertexArrayTexCoordOffsetEXT;
    public final long glVertexArrayMultiTexCoordOffsetEXT;
    public final long glVertexArrayFogCoordOffsetEXT;
    public final long glVertexArraySecondaryColorOffsetEXT;
    public final long glVertexArrayVertexAttribOffsetEXT;
    public final long glVertexArrayVertexAttribIOffsetEXT;
    public final long glEnableVertexArrayEXT;
    public final long glDisableVertexArrayEXT;
    public final long glEnableVertexArrayAttribEXT;
    public final long glDisableVertexArrayAttribEXT;
    public final long glGetVertexArrayIntegervEXT;
    public final long glGetVertexArrayPointervEXT;
    public final long glGetVertexArrayIntegeri_vEXT;
    public final long glGetVertexArrayPointeri_vEXT;
    public final long glMapNamedBufferRangeEXT;
    public final long glFlushMappedNamedBufferRangeEXT;
    public final long glColorMaskIndexedEXT;
    public final long glDrawArraysInstancedEXT;
    public final long glDrawElementsInstancedEXT;
    public final long glEGLImageTargetTexStorageEXT;
    public final long glEGLImageTargetTextureStorageEXT;
    public final long glBufferStorageExternalEXT;
    public final long glNamedBufferStorageExternalEXT;
    public final long glBlitFramebufferEXT;
    public final long glBlitFramebufferLayersEXT;
    public final long glBlitFramebufferLayerEXT;
    public final long glRenderbufferStorageMultisampleEXT;
    public final long glIsRenderbufferEXT;
    public final long glBindRenderbufferEXT;
    public final long glDeleteRenderbuffersEXT;
    public final long glGenRenderbuffersEXT;
    public final long glRenderbufferStorageEXT;
    public final long glGetRenderbufferParameterivEXT;
    public final long glIsFramebufferEXT;
    public final long glBindFramebufferEXT;
    public final long glDeleteFramebuffersEXT;
    public final long glGenFramebuffersEXT;
    public final long glCheckFramebufferStatusEXT;
    public final long glFramebufferTexture1DEXT;
    public final long glFramebufferTexture2DEXT;
    public final long glFramebufferTexture3DEXT;
    public final long glFramebufferRenderbufferEXT;
    public final long glGetFramebufferAttachmentParameterivEXT;
    public final long glGenerateMipmapEXT;
    public final long glProgramParameteriEXT;
    public final long glFramebufferTextureEXT;
    public final long glFramebufferTextureLayerEXT;
    public final long glFramebufferTextureFaceEXT;
    public final long glProgramEnvParameters4fvEXT;
    public final long glProgramLocalParameters4fvEXT;
    public final long glVertexAttribI1iEXT;
    public final long glVertexAttribI2iEXT;
    public final long glVertexAttribI3iEXT;
    public final long glVertexAttribI4iEXT;
    public final long glVertexAttribI1uiEXT;
    public final long glVertexAttribI2uiEXT;
    public final long glVertexAttribI3uiEXT;
    public final long glVertexAttribI4uiEXT;
    public final long glVertexAttribI1ivEXT;
    public final long glVertexAttribI2ivEXT;
    public final long glVertexAttribI3ivEXT;
    public final long glVertexAttribI4ivEXT;
    public final long glVertexAttribI1uivEXT;
    public final long glVertexAttribI2uivEXT;
    public final long glVertexAttribI3uivEXT;
    public final long glVertexAttribI4uivEXT;
    public final long glVertexAttribI4bvEXT;
    public final long glVertexAttribI4svEXT;
    public final long glVertexAttribI4ubvEXT;
    public final long glVertexAttribI4usvEXT;
    public final long glVertexAttribIPointerEXT;
    public final long glGetVertexAttribIivEXT;
    public final long glGetVertexAttribIuivEXT;
    public final long glGetUniformuivEXT;
    public final long glBindFragDataLocationEXT;
    public final long glGetFragDataLocationEXT;
    public final long glUniform1uiEXT;
    public final long glUniform2uiEXT;
    public final long glUniform3uiEXT;
    public final long glUniform4uiEXT;
    public final long glUniform1uivEXT;
    public final long glUniform2uivEXT;
    public final long glUniform3uivEXT;
    public final long glUniform4uivEXT;
    public final long glGetUnsignedBytevEXT;
    public final long glGetUnsignedBytei_vEXT;
    public final long glDeleteMemoryObjectsEXT;
    public final long glIsMemoryObjectEXT;
    public final long glCreateMemoryObjectsEXT;
    public final long glMemoryObjectParameterivEXT;
    public final long glGetMemoryObjectParameterivEXT;
    public final long glTexStorageMem2DEXT;
    public final long glTexStorageMem2DMultisampleEXT;
    public final long glTexStorageMem3DEXT;
    public final long glTexStorageMem3DMultisampleEXT;
    public final long glBufferStorageMemEXT;
    public final long glTextureStorageMem2DEXT;
    public final long glTextureStorageMem2DMultisampleEXT;
    public final long glTextureStorageMem3DEXT;
    public final long glTextureStorageMem3DMultisampleEXT;
    public final long glNamedBufferStorageMemEXT;
    public final long glTexStorageMem1DEXT;
    public final long glTextureStorageMem1DEXT;
    public final long glImportMemoryFdEXT;
    public final long glImportMemoryWin32HandleEXT;
    public final long glImportMemoryWin32NameEXT;
    public final long glPointParameterfEXT;
    public final long glPointParameterfvEXT;
    public final long glPolygonOffsetClampEXT;
    public final long glProvokingVertexEXT;
    public final long glRasterSamplesEXT;
    public final long glSecondaryColor3bEXT;
    public final long glSecondaryColor3sEXT;
    public final long glSecondaryColor3iEXT;
    public final long glSecondaryColor3fEXT;
    public final long glSecondaryColor3dEXT;
    public final long glSecondaryColor3ubEXT;
    public final long glSecondaryColor3usEXT;
    public final long glSecondaryColor3uiEXT;
    public final long glSecondaryColor3bvEXT;
    public final long glSecondaryColor3svEXT;
    public final long glSecondaryColor3ivEXT;
    public final long glSecondaryColor3fvEXT;
    public final long glSecondaryColor3dvEXT;
    public final long glSecondaryColor3ubvEXT;
    public final long glSecondaryColor3usvEXT;
    public final long glSecondaryColor3uivEXT;
    public final long glSecondaryColorPointerEXT;
    public final long glGenSemaphoresEXT;
    public final long glDeleteSemaphoresEXT;
    public final long glIsSemaphoreEXT;
    public final long glSemaphoreParameterui64vEXT;
    public final long glGetSemaphoreParameterui64vEXT;
    public final long glWaitSemaphoreEXT;
    public final long glSignalSemaphoreEXT;
    public final long glImportSemaphoreFdEXT;
    public final long glImportSemaphoreWin32HandleEXT;
    public final long glImportSemaphoreWin32NameEXT;
    public final long glUseShaderProgramEXT;
    public final long glActiveProgramEXT;
    public final long glCreateShaderProgramEXT;
    public final long glFramebufferFetchBarrierEXT;
    public final long glBindImageTextureEXT;
    public final long glMemoryBarrierEXT;
    public final long glStencilClearTagEXT;
    public final long glActiveStencilFaceEXT;
    public final long glTexBufferEXT;
    public final long glClearColorIiEXT;
    public final long glClearColorIuiEXT;
    public final long glTexParameterIivEXT;
    public final long glTexParameterIuivEXT;
    public final long glGetTexParameterIivEXT;
    public final long glGetTexParameterIuivEXT;
    public final long glTexStorage1DEXT;
    public final long glTexStorage2DEXT;
    public final long glTexStorage3DEXT;
    public final long glGetQueryObjecti64vEXT;
    public final long glGetQueryObjectui64vEXT;
    public final long glBindBufferRangeEXT;
    public final long glBindBufferOffsetEXT;
    public final long glBindBufferBaseEXT;
    public final long glBeginTransformFeedbackEXT;
    public final long glEndTransformFeedbackEXT;
    public final long glTransformFeedbackVaryingsEXT;
    public final long glGetTransformFeedbackVaryingEXT;
    public final long glVertexAttribL1dEXT;
    public final long glVertexAttribL2dEXT;
    public final long glVertexAttribL3dEXT;
    public final long glVertexAttribL4dEXT;
    public final long glVertexAttribL1dvEXT;
    public final long glVertexAttribL2dvEXT;
    public final long glVertexAttribL3dvEXT;
    public final long glVertexAttribL4dvEXT;
    public final long glVertexAttribLPointerEXT;
    public final long glGetVertexAttribLdvEXT;
    public final long glAcquireKeyedMutexWin32EXT;
    public final long glReleaseKeyedMutexWin32EXT;
    public final long glWindowRectanglesEXT;
    public final long glImportSyncEXT;
    public final long glFrameTerminatorGREMEDY;
    public final long glStringMarkerGREMEDY;
    public final long glApplyFramebufferAttachmentCMAAINTEL;
    public final long glSyncTextureINTEL;
    public final long glUnmapTexture2DINTEL;
    public final long glMapTexture2DINTEL;
    public final long glBeginPerfQueryINTEL;
    public final long glCreatePerfQueryINTEL;
    public final long glDeletePerfQueryINTEL;
    public final long glEndPerfQueryINTEL;
    public final long glGetFirstPerfQueryIdINTEL;
    public final long glGetNextPerfQueryIdINTEL;
    public final long glGetPerfCounterInfoINTEL;
    public final long glGetPerfQueryDataINTEL;
    public final long glGetPerfQueryIdByNameINTEL;
    public final long glGetPerfQueryInfoINTEL;
    public final long glBlendBarrierKHR;
    public final long glMaxShaderCompilerThreadsKHR;
    public final long glFramebufferParameteriMESA;
    public final long glGetFramebufferParameterivMESA;
    public final long glAlphaToCoverageDitherControlNV;
    public final long glMultiDrawArraysIndirectBindlessNV;
    public final long glMultiDrawElementsIndirectBindlessNV;
    public final long glMultiDrawArraysIndirectBindlessCountNV;
    public final long glMultiDrawElementsIndirectBindlessCountNV;
    public final long glGetTextureHandleNV;
    public final long glGetTextureSamplerHandleNV;
    public final long glMakeTextureHandleResidentNV;
    public final long glMakeTextureHandleNonResidentNV;
    public final long glGetImageHandleNV;
    public final long glMakeImageHandleResidentNV;
    public final long glMakeImageHandleNonResidentNV;
    public final long glUniformHandleui64NV;
    public final long glUniformHandleui64vNV;
    public final long glProgramUniformHandleui64NV;
    public final long glProgramUniformHandleui64vNV;
    public final long glIsTextureHandleResidentNV;
    public final long glIsImageHandleResidentNV;
    public final long glBlendParameteriNV;
    public final long glBlendBarrierNV;
    public final long glViewportPositionWScaleNV;
    public final long glCreateStatesNV;
    public final long glDeleteStatesNV;
    public final long glIsStateNV;
    public final long glStateCaptureNV;
    public final long glGetCommandHeaderNV;
    public final long glGetStageIndexNV;
    public final long glDrawCommandsNV;
    public final long glDrawCommandsAddressNV;
    public final long glDrawCommandsStatesNV;
    public final long glDrawCommandsStatesAddressNV;
    public final long glCreateCommandListsNV;
    public final long glDeleteCommandListsNV;
    public final long glIsCommandListNV;
    public final long glListDrawCommandsStatesClientNV;
    public final long glCommandListSegmentsNV;
    public final long glCompileCommandListNV;
    public final long glCallCommandListNV;
    public final long glBeginConditionalRenderNV;
    public final long glEndConditionalRenderNV;
    public final long glSubpixelPrecisionBiasNV;
    public final long glConservativeRasterParameterfNV;
    public final long glConservativeRasterParameteriNV;
    public final long glCopyImageSubDataNV;
    public final long glDepthRangedNV;
    public final long glClearDepthdNV;
    public final long glDepthBoundsdNV;
    public final long glDrawTextureNV;
    public final long glDrawVkImageNV;
    public final long glGetVkProcAddrNV;
    public final long glWaitVkSemaphoreNV;
    public final long glSignalVkSemaphoreNV;
    public final long glSignalVkFenceNV;
    public final long glGetMultisamplefvNV;
    public final long glSampleMaskIndexedNV;
    public final long glTexRenderbufferNV;
    public final long glDeleteFencesNV;
    public final long glGenFencesNV;
    public final long glIsFenceNV;
    public final long glTestFenceNV;
    public final long glGetFenceivNV;
    public final long glFinishFenceNV;
    public final long glSetFenceNV;
    public final long glFragmentCoverageColorNV;
    public final long glCoverageModulationTableNV;
    public final long glGetCoverageModulationTableNV;
    public final long glCoverageModulationNV;
    public final long glRenderbufferStorageMultisampleCoverageNV;
    public final long glRenderGpuMaskNV;
    public final long glMulticastBufferSubDataNV;
    public final long glMulticastCopyBufferSubDataNV;
    public final long glMulticastCopyImageSubDataNV;
    public final long glMulticastBlitFramebufferNV;
    public final long glMulticastFramebufferSampleLocationsfvNV;
    public final long glMulticastBarrierNV;
    public final long glMulticastWaitSyncNV;
    public final long glMulticastGetQueryObjectivNV;
    public final long glMulticastGetQueryObjectuivNV;
    public final long glMulticastGetQueryObjecti64vNV;
    public final long glMulticastGetQueryObjectui64vNV;
    public final long glVertex2hNV;
    public final long glVertex2hvNV;
    public final long glVertex3hNV;
    public final long glVertex3hvNV;
    public final long glVertex4hNV;
    public final long glVertex4hvNV;
    public final long glNormal3hNV;
    public final long glNormal3hvNV;
    public final long glColor3hNV;
    public final long glColor3hvNV;
    public final long glColor4hNV;
    public final long glColor4hvNV;
    public final long glTexCoord1hNV;
    public final long glTexCoord1hvNV;
    public final long glTexCoord2hNV;
    public final long glTexCoord2hvNV;
    public final long glTexCoord3hNV;
    public final long glTexCoord3hvNV;
    public final long glTexCoord4hNV;
    public final long glTexCoord4hvNV;
    public final long glMultiTexCoord1hNV;
    public final long glMultiTexCoord1hvNV;
    public final long glMultiTexCoord2hNV;
    public final long glMultiTexCoord2hvNV;
    public final long glMultiTexCoord3hNV;
    public final long glMultiTexCoord3hvNV;
    public final long glMultiTexCoord4hNV;
    public final long glMultiTexCoord4hvNV;
    public final long glFogCoordhNV;
    public final long glFogCoordhvNV;
    public final long glSecondaryColor3hNV;
    public final long glSecondaryColor3hvNV;
    public final long glVertexWeighthNV;
    public final long glVertexWeighthvNV;
    public final long glVertexAttrib1hNV;
    public final long glVertexAttrib1hvNV;
    public final long glVertexAttrib2hNV;
    public final long glVertexAttrib2hvNV;
    public final long glVertexAttrib3hNV;
    public final long glVertexAttrib3hvNV;
    public final long glVertexAttrib4hNV;
    public final long glVertexAttrib4hvNV;
    public final long glVertexAttribs1hvNV;
    public final long glVertexAttribs2hvNV;
    public final long glVertexAttribs3hvNV;
    public final long glVertexAttribs4hvNV;
    public final long glGetInternalformatSampleivNV;
    public final long glGetMemoryObjectDetachedResourcesuivNV;
    public final long glResetMemoryObjectParameterNV;
    public final long glTexAttachMemoryNV;
    public final long glBufferAttachMemoryNV;
    public final long glTextureAttachMemoryNV;
    public final long glNamedBufferAttachMemoryNV;
    public final long glBufferPageCommitmentMemNV;
    public final long glNamedBufferPageCommitmentMemNV;
    public final long glTexPageCommitmentMemNV;
    public final long glTexturePageCommitmentMemNV;
    public final long glDrawMeshTasksNV;
    public final long glDrawMeshTasksIndirectNV;
    public final long glMultiDrawMeshTasksIndirectNV;
    public final long glMultiDrawMeshTasksIndirectCountNV;
    public final long glPathCommandsNV;
    public final long glPathCoordsNV;
    public final long glPathSubCommandsNV;
    public final long glPathSubCoordsNV;
    public final long glPathStringNV;
    public final long glPathGlyphsNV;
    public final long glPathGlyphRangeNV;
    public final long glPathGlyphIndexArrayNV;
    public final long glPathMemoryGlyphIndexArrayNV;
    public final long glCopyPathNV;
    public final long glWeightPathsNV;
    public final long glInterpolatePathsNV;
    public final long glTransformPathNV;
    public final long glPathParameterivNV;
    public final long glPathParameteriNV;
    public final long glPathParameterfvNV;
    public final long glPathParameterfNV;
    public final long glPathDashArrayNV;
    public final long glGenPathsNV;
    public final long glDeletePathsNV;
    public final long glIsPathNV;
    public final long glPathStencilFuncNV;
    public final long glPathStencilDepthOffsetNV;
    public final long glStencilFillPathNV;
    public final long glStencilStrokePathNV;
    public final long glStencilFillPathInstancedNV;
    public final long glStencilStrokePathInstancedNV;
    public final long glPathCoverDepthFuncNV;
    public final long glPathColorGenNV;
    public final long glPathTexGenNV;
    public final long glPathFogGenNV;
    public final long glCoverFillPathNV;
    public final long glCoverStrokePathNV;
    public final long glCoverFillPathInstancedNV;
    public final long glCoverStrokePathInstancedNV;
    public final long glStencilThenCoverFillPathNV;
    public final long glStencilThenCoverStrokePathNV;
    public final long glStencilThenCoverFillPathInstancedNV;
    public final long glStencilThenCoverStrokePathInstancedNV;
    public final long glPathGlyphIndexRangeNV;
    public final long glProgramPathFragmentInputGenNV;
    public final long glGetPathParameterivNV;
    public final long glGetPathParameterfvNV;
    public final long glGetPathCommandsNV;
    public final long glGetPathCoordsNV;
    public final long glGetPathDashArrayNV;
    public final long glGetPathMetricsNV;
    public final long glGetPathMetricRangeNV;
    public final long glGetPathSpacingNV;
    public final long glGetPathColorGenivNV;
    public final long glGetPathColorGenfvNV;
    public final long glGetPathTexGenivNV;
    public final long glGetPathTexGenfvNV;
    public final long glIsPointInFillPathNV;
    public final long glIsPointInStrokePathNV;
    public final long glGetPathLengthNV;
    public final long glPointAlongPathNV;
    public final long glMatrixLoad3x2fNV;
    public final long glMatrixLoad3x3fNV;
    public final long glMatrixLoadTranspose3x3fNV;
    public final long glMatrixMult3x2fNV;
    public final long glMatrixMult3x3fNV;
    public final long glMatrixMultTranspose3x3fNV;
    public final long glGetProgramResourcefvNV;
    public final long glPixelDataRangeNV;
    public final long glFlushPixelDataRangeNV;
    public final long glPointParameteriNV;
    public final long glPointParameterivNV;
    public final long glPrimitiveRestartNV;
    public final long glPrimitiveRestartIndexNV;
    public final long glQueryResourceNV;
    public final long glGenQueryResourceTagNV;
    public final long glDeleteQueryResourceTagNV;
    public final long glQueryResourceTagNV;
    public final long glFramebufferSampleLocationsfvNV;
    public final long glNamedFramebufferSampleLocationsfvNV;
    public final long glResolveDepthValuesNV;
    public final long glScissorExclusiveArrayvNV;
    public final long glScissorExclusiveNV;
    public final long glMakeBufferResidentNV;
    public final long glMakeBufferNonResidentNV;
    public final long glIsBufferResidentNV;
    public final long glMakeNamedBufferResidentNV;
    public final long glMakeNamedBufferNonResidentNV;
    public final long glIsNamedBufferResidentNV;
    public final long glGetBufferParameterui64vNV;
    public final long glGetNamedBufferParameterui64vNV;
    public final long glGetIntegerui64vNV;
    public final long glUniformui64NV;
    public final long glUniformui64vNV;
    public final long glProgramUniformui64NV;
    public final long glProgramUniformui64vNV;
    public final long glBindShadingRateImageNV;
    public final long glShadingRateImagePaletteNV;
    public final long glGetShadingRateImagePaletteNV;
    public final long glShadingRateImageBarrierNV;
    public final long glShadingRateSampleOrderNV;
    public final long glShadingRateSampleOrderCustomNV;
    public final long glGetShadingRateSampleLocationivNV;
    public final long glTextureBarrierNV;
    public final long glTexImage2DMultisampleCoverageNV;
    public final long glTexImage3DMultisampleCoverageNV;
    public final long glTextureImage2DMultisampleNV;
    public final long glTextureImage3DMultisampleNV;
    public final long glTextureImage2DMultisampleCoverageNV;
    public final long glTextureImage3DMultisampleCoverageNV;
    public final long glCreateSemaphoresNV;
    public final long glSemaphoreParameterivNV;
    public final long glGetSemaphoreParameterivNV;
    public final long glBeginTransformFeedbackNV;
    public final long glEndTransformFeedbackNV;
    public final long glTransformFeedbackAttribsNV;
    public final long glBindBufferRangeNV;
    public final long glBindBufferOffsetNV;
    public final long glBindBufferBaseNV;
    public final long glTransformFeedbackVaryingsNV;
    public final long glActiveVaryingNV;
    public final long glGetVaryingLocationNV;
    public final long glGetActiveVaryingNV;
    public final long glGetTransformFeedbackVaryingNV;
    public final long glTransformFeedbackStreamAttribsNV;
    public final long glBindTransformFeedbackNV;
    public final long glDeleteTransformFeedbacksNV;
    public final long glGenTransformFeedbacksNV;
    public final long glIsTransformFeedbackNV;
    public final long glPauseTransformFeedbackNV;
    public final long glResumeTransformFeedbackNV;
    public final long glDrawTransformFeedbackNV;
    public final long glVertexArrayRangeNV;
    public final long glFlushVertexArrayRangeNV;
    public final long glVertexAttribL1i64NV;
    public final long glVertexAttribL2i64NV;
    public final long glVertexAttribL3i64NV;
    public final long glVertexAttribL4i64NV;
    public final long glVertexAttribL1i64vNV;
    public final long glVertexAttribL2i64vNV;
    public final long glVertexAttribL3i64vNV;
    public final long glVertexAttribL4i64vNV;
    public final long glVertexAttribL1ui64NV;
    public final long glVertexAttribL2ui64NV;
    public final long glVertexAttribL3ui64NV;
    public final long glVertexAttribL4ui64NV;
    public final long glVertexAttribL1ui64vNV;
    public final long glVertexAttribL2ui64vNV;
    public final long glVertexAttribL3ui64vNV;
    public final long glVertexAttribL4ui64vNV;
    public final long glGetVertexAttribLi64vNV;
    public final long glGetVertexAttribLui64vNV;
    public final long glVertexAttribLFormatNV;
    public final long glBufferAddressRangeNV;
    public final long glVertexFormatNV;
    public final long glNormalFormatNV;
    public final long glColorFormatNV;
    public final long glIndexFormatNV;
    public final long glTexCoordFormatNV;
    public final long glEdgeFlagFormatNV;
    public final long glSecondaryColorFormatNV;
    public final long glFogCoordFormatNV;
    public final long glVertexAttribFormatNV;
    public final long glVertexAttribIFormatNV;
    public final long glGetIntegerui64i_vNV;
    public final long glViewportSwizzleNV;
    public final long glBeginConditionalRenderNVX;
    public final long glEndConditionalRenderNVX;
    public final long glAsyncCopyImageSubDataNVX;
    public final long glAsyncCopyBufferSubDataNVX;
    public final long glUploadGpuMaskNVX;
    public final long glMulticastViewportArrayvNVX;
    public final long glMulticastScissorArrayvNVX;
    public final long glMulticastViewportPositionWScaleNVX;
    public final long glCreateProgressFenceNVX;
    public final long glSignalSemaphoreui64NVX;
    public final long glWaitSemaphoreui64NVX;
    public final long glClientWaitSemaphoreui64NVX;
    public final long glFramebufferTextureMultiviewOVR;
    public final long glNamedFramebufferTextureMultiviewOVR;
    public final boolean OpenGL11;
    public final boolean OpenGL12;
    public final boolean OpenGL13;
    public final boolean OpenGL14;
    public final boolean OpenGL15;
    public final boolean OpenGL20;
    public final boolean OpenGL21;
    public final boolean OpenGL30;
    public final boolean OpenGL31;
    public final boolean OpenGL32;
    public final boolean OpenGL33;
    public final boolean OpenGL40;
    public final boolean OpenGL41;
    public final boolean OpenGL42;
    public final boolean OpenGL43;
    public final boolean OpenGL44;
    public final boolean OpenGL45;
    public final boolean OpenGL46;
    public final boolean GL_3DFX_texture_compression_FXT1;
    public final boolean GL_AMD_blend_minmax_factor;
    public final boolean GL_AMD_conservative_depth;
    public final boolean GL_AMD_debug_output;
    public final boolean GL_AMD_depth_clamp_separate;
    public final boolean GL_AMD_draw_buffers_blend;
    public final boolean GL_AMD_framebuffer_multisample_advanced;
    public final boolean GL_AMD_gcn_shader;
    public final boolean GL_AMD_gpu_shader_half_float;
    public final boolean GL_AMD_gpu_shader_half_float_fetch;
    public final boolean GL_AMD_gpu_shader_int16;
    public final boolean GL_AMD_gpu_shader_int64;
    public final boolean GL_AMD_interleaved_elements;
    public final boolean GL_AMD_occlusion_query_event;
    public final boolean GL_AMD_performance_monitor;
    public final boolean GL_AMD_pinned_memory;
    public final boolean GL_AMD_query_buffer_object;
    public final boolean GL_AMD_sample_positions;
    public final boolean GL_AMD_seamless_cubemap_per_texture;
    public final boolean GL_AMD_shader_atomic_counter_ops;
    public final boolean GL_AMD_shader_ballot;
    public final boolean GL_AMD_shader_explicit_vertex_parameter;
    public final boolean GL_AMD_shader_image_load_store_lod;
    public final boolean GL_AMD_shader_stencil_export;
    public final boolean GL_AMD_shader_trinary_minmax;
    public final boolean GL_AMD_sparse_texture;
    public final boolean GL_AMD_stencil_operation_extended;
    public final boolean GL_AMD_texture_gather_bias_lod;
    public final boolean GL_AMD_texture_texture4;
    public final boolean GL_AMD_transform_feedback3_lines_triangles;
    public final boolean GL_AMD_transform_feedback4;
    public final boolean GL_AMD_vertex_shader_layer;
    public final boolean GL_AMD_vertex_shader_tessellator;
    public final boolean GL_AMD_vertex_shader_viewport_index;
    public final boolean GL_ARB_arrays_of_arrays;
    public final boolean GL_ARB_base_instance;
    public final boolean GL_ARB_bindless_texture;
    public final boolean GL_ARB_blend_func_extended;
    public final boolean GL_ARB_buffer_storage;
    public final boolean GL_ARB_cl_event;
    public final boolean GL_ARB_clear_buffer_object;
    public final boolean GL_ARB_clear_texture;
    public final boolean GL_ARB_clip_control;
    public final boolean GL_ARB_color_buffer_float;
    public final boolean GL_ARB_compatibility;
    public final boolean GL_ARB_compressed_texture_pixel_storage;
    public final boolean GL_ARB_compute_shader;
    public final boolean GL_ARB_compute_variable_group_size;
    public final boolean GL_ARB_conditional_render_inverted;
    public final boolean GL_ARB_conservative_depth;
    public final boolean GL_ARB_copy_buffer;
    public final boolean GL_ARB_copy_image;
    public final boolean GL_ARB_cull_distance;
    public final boolean GL_ARB_debug_output;
    public final boolean GL_ARB_depth_buffer_float;
    public final boolean GL_ARB_depth_clamp;
    public final boolean GL_ARB_depth_texture;
    public final boolean GL_ARB_derivative_control;
    public final boolean GL_ARB_direct_state_access;
    public final boolean GL_ARB_draw_buffers;
    public final boolean GL_ARB_draw_buffers_blend;
    public final boolean GL_ARB_draw_elements_base_vertex;
    public final boolean GL_ARB_draw_indirect;
    public final boolean GL_ARB_draw_instanced;
    public final boolean GL_ARB_enhanced_layouts;
    public final boolean GL_ARB_ES2_compatibility;
    public final boolean GL_ARB_ES3_1_compatibility;
    public final boolean GL_ARB_ES3_2_compatibility;
    public final boolean GL_ARB_ES3_compatibility;
    public final boolean GL_ARB_explicit_attrib_location;
    public final boolean GL_ARB_explicit_uniform_location;
    public final boolean GL_ARB_fragment_coord_conventions;
    public final boolean GL_ARB_fragment_layer_viewport;
    public final boolean GL_ARB_fragment_program;
    public final boolean GL_ARB_fragment_program_shadow;
    public final boolean GL_ARB_fragment_shader;
    public final boolean GL_ARB_fragment_shader_interlock;
    public final boolean GL_ARB_framebuffer_no_attachments;
    public final boolean GL_ARB_framebuffer_object;
    public final boolean GL_ARB_framebuffer_sRGB;
    public final boolean GL_ARB_geometry_shader4;
    public final boolean GL_ARB_get_program_binary;
    public final boolean GL_ARB_get_texture_sub_image;
    public final boolean GL_ARB_gl_spirv;
    public final boolean GL_ARB_gpu_shader5;
    public final boolean GL_ARB_gpu_shader_fp64;
    public final boolean GL_ARB_gpu_shader_int64;
    public final boolean GL_ARB_half_float_pixel;
    public final boolean GL_ARB_half_float_vertex;
    public final boolean GL_ARB_imaging;
    public final boolean GL_ARB_indirect_parameters;
    public final boolean GL_ARB_instanced_arrays;
    public final boolean GL_ARB_internalformat_query;
    public final boolean GL_ARB_internalformat_query2;
    public final boolean GL_ARB_invalidate_subdata;
    public final boolean GL_ARB_map_buffer_alignment;
    public final boolean GL_ARB_map_buffer_range;
    public final boolean GL_ARB_matrix_palette;
    public final boolean GL_ARB_multi_bind;
    public final boolean GL_ARB_multi_draw_indirect;
    public final boolean GL_ARB_multisample;
    public final boolean GL_ARB_multitexture;
    public final boolean GL_ARB_occlusion_query;
    public final boolean GL_ARB_occlusion_query2;
    public final boolean GL_ARB_parallel_shader_compile;
    public final boolean GL_ARB_pipeline_statistics_query;
    public final boolean GL_ARB_pixel_buffer_object;
    public final boolean GL_ARB_point_parameters;
    public final boolean GL_ARB_point_sprite;
    public final boolean GL_ARB_polygon_offset_clamp;
    public final boolean GL_ARB_post_depth_coverage;
    public final boolean GL_ARB_program_interface_query;
    public final boolean GL_ARB_provoking_vertex;
    public final boolean GL_ARB_query_buffer_object;
    public final boolean GL_ARB_robust_buffer_access_behavior;
    public final boolean GL_ARB_robustness;
    public final boolean GL_ARB_robustness_application_isolation;
    public final boolean GL_ARB_robustness_share_group_isolation;
    public final boolean GL_ARB_sample_locations;
    public final boolean GL_ARB_sample_shading;
    public final boolean GL_ARB_sampler_objects;
    public final boolean GL_ARB_seamless_cube_map;
    public final boolean GL_ARB_seamless_cubemap_per_texture;
    public final boolean GL_ARB_separate_shader_objects;
    public final boolean GL_ARB_shader_atomic_counter_ops;
    public final boolean GL_ARB_shader_atomic_counters;
    public final boolean GL_ARB_shader_ballot;
    public final boolean GL_ARB_shader_bit_encoding;
    public final boolean GL_ARB_shader_clock;
    public final boolean GL_ARB_shader_draw_parameters;
    public final boolean GL_ARB_shader_group_vote;
    public final boolean GL_ARB_shader_image_load_store;
    public final boolean GL_ARB_shader_image_size;
    public final boolean GL_ARB_shader_objects;
    public final boolean GL_ARB_shader_precision;
    public final boolean GL_ARB_shader_stencil_export;
    public final boolean GL_ARB_shader_storage_buffer_object;
    public final boolean GL_ARB_shader_subroutine;
    public final boolean GL_ARB_shader_texture_image_samples;
    public final boolean GL_ARB_shader_texture_lod;
    public final boolean GL_ARB_shader_viewport_layer_array;
    public final boolean GL_ARB_shading_language_100;
    public final boolean GL_ARB_shading_language_420pack;
    public final boolean GL_ARB_shading_language_include;
    public final boolean GL_ARB_shading_language_packing;
    public final boolean GL_ARB_shadow;
    public final boolean GL_ARB_shadow_ambient;
    public final boolean GL_ARB_sparse_buffer;
    public final boolean GL_ARB_sparse_texture;
    public final boolean GL_ARB_sparse_texture2;
    public final boolean GL_ARB_sparse_texture_clamp;
    public final boolean GL_ARB_spirv_extensions;
    public final boolean GL_ARB_stencil_texturing;
    public final boolean GL_ARB_sync;
    public final boolean GL_ARB_tessellation_shader;
    public final boolean GL_ARB_texture_barrier;
    public final boolean GL_ARB_texture_border_clamp;
    public final boolean GL_ARB_texture_buffer_object;
    public final boolean GL_ARB_texture_buffer_object_rgb32;
    public final boolean GL_ARB_texture_buffer_range;
    public final boolean GL_ARB_texture_compression;
    public final boolean GL_ARB_texture_compression_bptc;
    public final boolean GL_ARB_texture_compression_rgtc;
    public final boolean GL_ARB_texture_cube_map;
    public final boolean GL_ARB_texture_cube_map_array;
    public final boolean GL_ARB_texture_env_add;
    public final boolean GL_ARB_texture_env_combine;
    public final boolean GL_ARB_texture_env_crossbar;
    public final boolean GL_ARB_texture_env_dot3;
    public final boolean GL_ARB_texture_filter_anisotropic;
    public final boolean GL_ARB_texture_filter_minmax;
    public final boolean GL_ARB_texture_float;
    public final boolean GL_ARB_texture_gather;
    public final boolean GL_ARB_texture_mirror_clamp_to_edge;
    public final boolean GL_ARB_texture_mirrored_repeat;
    public final boolean GL_ARB_texture_multisample;
    public final boolean GL_ARB_texture_non_power_of_two;
    public final boolean GL_ARB_texture_query_levels;
    public final boolean GL_ARB_texture_query_lod;
    public final boolean GL_ARB_texture_rectangle;
    public final boolean GL_ARB_texture_rg;
    public final boolean GL_ARB_texture_rgb10_a2ui;
    public final boolean GL_ARB_texture_stencil8;
    public final boolean GL_ARB_texture_storage;
    public final boolean GL_ARB_texture_storage_multisample;
    public final boolean GL_ARB_texture_swizzle;
    public final boolean GL_ARB_texture_view;
    public final boolean GL_ARB_timer_query;
    public final boolean GL_ARB_transform_feedback2;
    public final boolean GL_ARB_transform_feedback3;
    public final boolean GL_ARB_transform_feedback_instanced;
    public final boolean GL_ARB_transform_feedback_overflow_query;
    public final boolean GL_ARB_transpose_matrix;
    public final boolean GL_ARB_uniform_buffer_object;
    public final boolean GL_ARB_vertex_array_bgra;
    public final boolean GL_ARB_vertex_array_object;
    public final boolean GL_ARB_vertex_attrib_64bit;
    public final boolean GL_ARB_vertex_attrib_binding;
    public final boolean GL_ARB_vertex_blend;
    public final boolean GL_ARB_vertex_buffer_object;
    public final boolean GL_ARB_vertex_program;
    public final boolean GL_ARB_vertex_shader;
    public final boolean GL_ARB_vertex_type_10f_11f_11f_rev;
    public final boolean GL_ARB_vertex_type_2_10_10_10_rev;
    public final boolean GL_ARB_viewport_array;
    public final boolean GL_ARB_window_pos;
    public final boolean GL_ATI_meminfo;
    public final boolean GL_ATI_shader_texture_lod;
    public final boolean GL_ATI_texture_compression_3dc;
    public final boolean GL_EXT_422_pixels;
    public final boolean GL_EXT_abgr;
    public final boolean GL_EXT_bgra;
    public final boolean GL_EXT_bindable_uniform;
    public final boolean GL_EXT_blend_color;
    public final boolean GL_EXT_blend_equation_separate;
    public final boolean GL_EXT_blend_func_separate;
    public final boolean GL_EXT_blend_minmax;
    public final boolean GL_EXT_blend_subtract;
    public final boolean GL_EXT_clip_volume_hint;
    public final boolean GL_EXT_compiled_vertex_array;
    public final boolean GL_EXT_debug_label;
    public final boolean GL_EXT_debug_marker;
    public final boolean GL_EXT_depth_bounds_test;
    public final boolean GL_EXT_direct_state_access;
    public final boolean GL_EXT_draw_buffers2;
    public final boolean GL_EXT_draw_instanced;
    public final boolean GL_EXT_EGL_image_storage;
    public final boolean GL_EXT_EGL_sync;
    public final boolean GL_EXT_external_buffer;
    public final boolean GL_EXT_framebuffer_blit;
    public final boolean GL_EXT_framebuffer_blit_layers;
    public final boolean GL_EXT_framebuffer_multisample;
    public final boolean GL_EXT_framebuffer_multisample_blit_scaled;
    public final boolean GL_EXT_framebuffer_object;
    public final boolean GL_EXT_framebuffer_sRGB;
    public final boolean GL_EXT_geometry_shader4;
    public final boolean GL_EXT_gpu_program_parameters;
    public final boolean GL_EXT_gpu_shader4;
    public final boolean GL_EXT_memory_object;
    public final boolean GL_EXT_memory_object_fd;
    public final boolean GL_EXT_memory_object_win32;
    public final boolean GL_EXT_multiview_tessellation_geometry_shader;
    public final boolean GL_EXT_multiview_texture_multisample;
    public final boolean GL_EXT_multiview_timer_query;
    public final boolean GL_EXT_packed_depth_stencil;
    public final boolean GL_EXT_packed_float;
    public final boolean GL_EXT_pixel_buffer_object;
    public final boolean GL_EXT_point_parameters;
    public final boolean GL_EXT_polygon_offset_clamp;
    public final boolean GL_EXT_post_depth_coverage;
    public final boolean GL_EXT_provoking_vertex;
    public final boolean GL_EXT_raster_multisample;
    public final boolean GL_EXT_secondary_color;
    public final boolean GL_EXT_semaphore;
    public final boolean GL_EXT_semaphore_fd;
    public final boolean GL_EXT_semaphore_win32;
    public final boolean GL_EXT_separate_shader_objects;
    public final boolean GL_EXT_shader_framebuffer_fetch;
    public final boolean GL_EXT_shader_framebuffer_fetch_non_coherent;
    public final boolean GL_EXT_shader_image_load_formatted;
    public final boolean GL_EXT_shader_image_load_store;
    public final boolean GL_EXT_shader_integer_mix;
    public final boolean GL_EXT_shader_samples_identical;
    public final boolean GL_EXT_shadow_funcs;
    public final boolean GL_EXT_shared_texture_palette;
    public final boolean GL_EXT_sparse_texture2;
    public final boolean GL_EXT_stencil_clear_tag;
    public final boolean GL_EXT_stencil_two_side;
    public final boolean GL_EXT_stencil_wrap;
    public final boolean GL_EXT_texture_array;
    public final boolean GL_EXT_texture_buffer_object;
    public final boolean GL_EXT_texture_compression_latc;
    public final boolean GL_EXT_texture_compression_rgtc;
    public final boolean GL_EXT_texture_compression_s3tc;
    public final boolean GL_EXT_texture_filter_anisotropic;
    public final boolean GL_EXT_texture_filter_minmax;
    public final boolean GL_EXT_texture_integer;
    public final boolean GL_EXT_texture_mirror_clamp;
    public final boolean GL_EXT_texture_shadow_lod;
    public final boolean GL_EXT_texture_shared_exponent;
    public final boolean GL_EXT_texture_snorm;
    public final boolean GL_EXT_texture_sRGB;
    public final boolean GL_EXT_texture_sRGB_decode;
    public final boolean GL_EXT_texture_sRGB_R8;
    public final boolean GL_EXT_texture_sRGB_RG8;
    public final boolean GL_EXT_texture_storage;
    public final boolean GL_EXT_texture_swizzle;
    public final boolean GL_EXT_timer_query;
    public final boolean GL_EXT_transform_feedback;
    public final boolean GL_EXT_vertex_array_bgra;
    public final boolean GL_EXT_vertex_attrib_64bit;
    public final boolean GL_EXT_win32_keyed_mutex;
    public final boolean GL_EXT_window_rectangles;
    public final boolean GL_EXT_x11_sync_object;
    public final boolean GL_GREMEDY_frame_terminator;
    public final boolean GL_GREMEDY_string_marker;
    public final boolean GL_INTEL_blackhole_render;
    public final boolean GL_INTEL_conservative_rasterization;
    public final boolean GL_INTEL_fragment_shader_ordering;
    public final boolean GL_INTEL_framebuffer_CMAA;
    public final boolean GL_INTEL_map_texture;
    public final boolean GL_INTEL_performance_query;
    public final boolean GL_INTEL_shader_integer_functions2;
    public final boolean GL_KHR_blend_equation_advanced;
    public final boolean GL_KHR_blend_equation_advanced_coherent;
    public final boolean GL_KHR_context_flush_control;
    public final boolean GL_KHR_debug;
    public final boolean GL_KHR_no_error;
    public final boolean GL_KHR_parallel_shader_compile;
    public final boolean GL_KHR_robust_buffer_access_behavior;
    public final boolean GL_KHR_robustness;
    public final boolean GL_KHR_shader_subgroup;
    public final boolean GL_KHR_texture_compression_astc_hdr;
    public final boolean GL_KHR_texture_compression_astc_ldr;
    public final boolean GL_KHR_texture_compression_astc_sliced_3d;
    public final boolean GL_MESA_framebuffer_flip_x;
    public final boolean GL_MESA_framebuffer_flip_y;
    public final boolean GL_MESA_framebuffer_swap_xy;
    public final boolean GL_MESA_tile_raster_order;
    public final boolean GL_NV_alpha_to_coverage_dither_control;
    public final boolean GL_NV_bindless_multi_draw_indirect;
    public final boolean GL_NV_bindless_multi_draw_indirect_count;
    public final boolean GL_NV_bindless_texture;
    public final boolean GL_NV_blend_equation_advanced;
    public final boolean GL_NV_blend_equation_advanced_coherent;
    public final boolean GL_NV_blend_minmax_factor;
    public final boolean GL_NV_blend_square;
    public final boolean GL_NV_clip_space_w_scaling;
    public final boolean GL_NV_command_list;
    public final boolean GL_NV_compute_shader_derivatives;
    public final boolean GL_NV_conditional_render;
    public final boolean GL_NV_conservative_raster;
    public final boolean GL_NV_conservative_raster_dilate;
    public final boolean GL_NV_conservative_raster_pre_snap;
    public final boolean GL_NV_conservative_raster_pre_snap_triangles;
    public final boolean GL_NV_conservative_raster_underestimation;
    public final boolean GL_NV_copy_depth_to_color;
    public final boolean GL_NV_copy_image;
    public final boolean GL_NV_deep_texture3D;
    public final boolean GL_NV_depth_buffer_float;
    public final boolean GL_NV_depth_clamp;
    public final boolean GL_NV_draw_texture;
    public final boolean GL_NV_draw_vulkan_image;
    public final boolean GL_NV_ES3_1_compatibility;
    public final boolean GL_NV_explicit_multisample;
    public final boolean GL_NV_fence;
    public final boolean GL_NV_fill_rectangle;
    public final boolean GL_NV_float_buffer;
    public final boolean GL_NV_fog_distance;
    public final boolean GL_NV_fragment_coverage_to_color;
    public final boolean GL_NV_fragment_program4;
    public final boolean GL_NV_fragment_program_option;
    public final boolean GL_NV_fragment_shader_barycentric;
    public final boolean GL_NV_fragment_shader_interlock;
    public final boolean GL_NV_framebuffer_mixed_samples;
    public final boolean GL_NV_framebuffer_multisample_coverage;
    public final boolean GL_NV_geometry_shader4;
    public final boolean GL_NV_geometry_shader_passthrough;
    public final boolean GL_NV_gpu_multicast;
    public final boolean GL_NV_gpu_shader5;
    public final boolean GL_NV_half_float;
    public final boolean GL_NV_internalformat_sample_query;
    public final boolean GL_NV_light_max_exponent;
    public final boolean GL_NV_memory_attachment;
    public final boolean GL_NV_memory_object_sparse;
    public final boolean GL_NV_mesh_shader;
    public final boolean GL_NV_multisample_coverage;
    public final boolean GL_NV_multisample_filter_hint;
    public final boolean GL_NV_packed_depth_stencil;
    public final boolean GL_NV_path_rendering;
    public final boolean GL_NV_path_rendering_shared_edge;
    public final boolean GL_NV_pixel_data_range;
    public final boolean GL_NV_point_sprite;
    public final boolean GL_NV_primitive_restart;
    public final boolean GL_NV_primitive_shading_rate;
    public final boolean GL_NV_query_resource;
    public final boolean GL_NV_query_resource_tag;
    public final boolean GL_NV_representative_fragment_test;
    public final boolean GL_NV_robustness_video_memory_purge;
    public final boolean GL_NV_sample_locations;
    public final boolean GL_NV_sample_mask_override_coverage;
    public final boolean GL_NV_scissor_exclusive;
    public final boolean GL_NV_shader_atomic_float;
    public final boolean GL_NV_shader_atomic_float64;
    public final boolean GL_NV_shader_atomic_fp16_vector;
    public final boolean GL_NV_shader_atomic_int64;
    public final boolean GL_NV_shader_buffer_load;
    public final boolean GL_NV_shader_buffer_store;
    public final boolean GL_NV_shader_subgroup_partitioned;
    public final boolean GL_NV_shader_texture_footprint;
    public final boolean GL_NV_shader_thread_group;
    public final boolean GL_NV_shader_thread_shuffle;
    public final boolean GL_NV_shading_rate_image;
    public final boolean GL_NV_stereo_view_rendering;
    public final boolean GL_NV_texgen_reflection;
    public final boolean GL_NV_texture_barrier;
    public final boolean GL_NV_texture_compression_vtc;
    public final boolean GL_NV_texture_multisample;
    public final boolean GL_NV_texture_rectangle_compressed;
    public final boolean GL_NV_texture_shader;
    public final boolean GL_NV_texture_shader2;
    public final boolean GL_NV_texture_shader3;
    public final boolean GL_NV_timeline_semaphore;
    public final boolean GL_NV_transform_feedback;
    public final boolean GL_NV_transform_feedback2;
    public final boolean GL_NV_uniform_buffer_std430_layout;
    public final boolean GL_NV_uniform_buffer_unified_memory;
    public final boolean GL_NV_vertex_array_range;
    public final boolean GL_NV_vertex_array_range2;
    public final boolean GL_NV_vertex_attrib_integer_64bit;
    public final boolean GL_NV_vertex_buffer_unified_memory;
    public final boolean GL_NV_viewport_array2;
    public final boolean GL_NV_viewport_swizzle;
    public final boolean GL_NVX_blend_equation_advanced_multi_draw_buffers;
    public final boolean GL_NVX_conditional_render;
    public final boolean GL_NVX_gpu_memory_info;
    public final boolean GL_NVX_gpu_multicast2;
    public final boolean GL_NVX_progress_fence;
    public final boolean GL_OVR_multiview;
    public final boolean GL_OVR_multiview2;
    public final boolean GL_S3_s3tc;
    public final boolean forwardCompatible;
    final PointerBuffer addresses;

    /*
     * Opcode count of 13566 triggered aggressive code reduction.  Override with --aggressivesizethreshold.
     */
    GLCapabilities(FunctionProvider provider, Set<String> ext, boolean fc, IntFunction<PointerBuffer> bufferFactory) {
        this.forwardCompatible = fc;
        PointerBuffer caps = bufferFactory.apply(2228);
        this.OpenGL11 = GLCapabilities.check_GL11(provider, caps, ext, fc);
        this.OpenGL12 = GLCapabilities.check_GL12(provider, caps, ext);
        this.OpenGL13 = GLCapabilities.check_GL13(provider, caps, ext, fc);
        this.OpenGL14 = GLCapabilities.check_GL14(provider, caps, ext, fc);
        this.OpenGL15 = GLCapabilities.check_GL15(provider, caps, ext);
        this.OpenGL20 = GLCapabilities.check_GL20(provider, caps, ext);
        this.OpenGL21 = GLCapabilities.check_GL21(provider, caps, ext);
        this.OpenGL30 = GLCapabilities.check_GL30(provider, caps, ext);
        this.OpenGL31 = GLCapabilities.check_GL31(provider, caps, ext);
        this.OpenGL32 = GLCapabilities.check_GL32(provider, caps, ext);
        this.OpenGL33 = GLCapabilities.check_GL33(provider, caps, ext, fc);
        this.OpenGL40 = GLCapabilities.check_GL40(provider, caps, ext);
        this.OpenGL41 = GLCapabilities.check_GL41(provider, caps, ext);
        this.OpenGL42 = GLCapabilities.check_GL42(provider, caps, ext);
        this.OpenGL43 = GLCapabilities.check_GL43(provider, caps, ext);
        this.OpenGL44 = GLCapabilities.check_GL44(provider, caps, ext);
        this.OpenGL45 = GLCapabilities.check_GL45(provider, caps, ext);
        this.OpenGL46 = GLCapabilities.check_GL46(provider, caps, ext);
        this.GL_3DFX_texture_compression_FXT1 = ext.contains("GL_3DFX_texture_compression_FXT1");
        this.GL_AMD_blend_minmax_factor = ext.contains("GL_AMD_blend_minmax_factor");
        this.GL_AMD_conservative_depth = ext.contains("GL_AMD_conservative_depth");
        this.GL_AMD_debug_output = GLCapabilities.check_AMD_debug_output(provider, caps, ext);
        this.GL_AMD_depth_clamp_separate = ext.contains("GL_AMD_depth_clamp_separate");
        this.GL_AMD_draw_buffers_blend = GLCapabilities.check_AMD_draw_buffers_blend(provider, caps, ext);
        this.GL_AMD_framebuffer_multisample_advanced = GLCapabilities.check_AMD_framebuffer_multisample_advanced(provider, caps, ext);
        this.GL_AMD_gcn_shader = ext.contains("GL_AMD_gcn_shader");
        this.GL_AMD_gpu_shader_half_float = ext.contains("GL_AMD_gpu_shader_half_float");
        this.GL_AMD_gpu_shader_half_float_fetch = ext.contains("GL_AMD_gpu_shader_half_float_fetch");
        this.GL_AMD_gpu_shader_int16 = ext.contains("GL_AMD_gpu_shader_int16");
        this.GL_AMD_gpu_shader_int64 = GLCapabilities.check_AMD_gpu_shader_int64(provider, caps, ext);
        this.GL_AMD_interleaved_elements = GLCapabilities.check_AMD_interleaved_elements(provider, caps, ext);
        this.GL_AMD_occlusion_query_event = GLCapabilities.check_AMD_occlusion_query_event(provider, caps, ext);
        this.GL_AMD_performance_monitor = GLCapabilities.check_AMD_performance_monitor(provider, caps, ext);
        this.GL_AMD_pinned_memory = ext.contains("GL_AMD_pinned_memory");
        this.GL_AMD_query_buffer_object = ext.contains("GL_AMD_query_buffer_object");
        this.GL_AMD_sample_positions = GLCapabilities.check_AMD_sample_positions(provider, caps, ext);
        this.GL_AMD_seamless_cubemap_per_texture = ext.contains("GL_AMD_seamless_cubemap_per_texture");
        this.GL_AMD_shader_atomic_counter_ops = ext.contains("GL_AMD_shader_atomic_counter_ops");
        this.GL_AMD_shader_ballot = ext.contains("GL_AMD_shader_ballot");
        this.GL_AMD_shader_explicit_vertex_parameter = ext.contains("GL_AMD_shader_explicit_vertex_parameter");
        this.GL_AMD_shader_image_load_store_lod = ext.contains("GL_AMD_shader_image_load_store_lod");
        this.GL_AMD_shader_stencil_export = ext.contains("GL_AMD_shader_stencil_export");
        this.GL_AMD_shader_trinary_minmax = ext.contains("GL_AMD_shader_trinary_minmax");
        this.GL_AMD_sparse_texture = GLCapabilities.check_AMD_sparse_texture(provider, caps, ext);
        this.GL_AMD_stencil_operation_extended = GLCapabilities.check_AMD_stencil_operation_extended(provider, caps, ext);
        this.GL_AMD_texture_gather_bias_lod = ext.contains("GL_AMD_texture_gather_bias_lod");
        this.GL_AMD_texture_texture4 = ext.contains("GL_AMD_texture_texture4");
        this.GL_AMD_transform_feedback3_lines_triangles = ext.contains("GL_AMD_transform_feedback3_lines_triangles");
        this.GL_AMD_transform_feedback4 = ext.contains("GL_AMD_transform_feedback4");
        this.GL_AMD_vertex_shader_layer = ext.contains("GL_AMD_vertex_shader_layer");
        this.GL_AMD_vertex_shader_tessellator = GLCapabilities.check_AMD_vertex_shader_tessellator(provider, caps, ext);
        this.GL_AMD_vertex_shader_viewport_index = ext.contains("GL_AMD_vertex_shader_viewport_index");
        this.GL_ARB_arrays_of_arrays = ext.contains("GL_ARB_arrays_of_arrays");
        this.GL_ARB_base_instance = GLCapabilities.check_ARB_base_instance(provider, caps, ext);
        this.GL_ARB_bindless_texture = GLCapabilities.check_ARB_bindless_texture(provider, caps, ext);
        this.GL_ARB_blend_func_extended = GLCapabilities.check_ARB_blend_func_extended(provider, caps, ext);
        this.GL_ARB_buffer_storage = GLCapabilities.check_ARB_buffer_storage(provider, caps, ext);
        this.GL_ARB_cl_event = GLCapabilities.check_ARB_cl_event(provider, caps, ext);
        this.GL_ARB_clear_buffer_object = GLCapabilities.check_ARB_clear_buffer_object(provider, caps, ext);
        this.GL_ARB_clear_texture = GLCapabilities.check_ARB_clear_texture(provider, caps, ext);
        this.GL_ARB_clip_control = GLCapabilities.check_ARB_clip_control(provider, caps, ext);
        this.GL_ARB_color_buffer_float = GLCapabilities.check_ARB_color_buffer_float(provider, caps, ext);
        this.GL_ARB_compatibility = ext.contains("GL_ARB_compatibility");
        this.GL_ARB_compressed_texture_pixel_storage = ext.contains("GL_ARB_compressed_texture_pixel_storage");
        this.GL_ARB_compute_shader = GLCapabilities.check_ARB_compute_shader(provider, caps, ext);
        this.GL_ARB_compute_variable_group_size = GLCapabilities.check_ARB_compute_variable_group_size(provider, caps, ext);
        this.GL_ARB_conditional_render_inverted = ext.contains("GL_ARB_conditional_render_inverted");
        this.GL_ARB_conservative_depth = ext.contains("GL_ARB_conservative_depth");
        this.GL_ARB_copy_buffer = GLCapabilities.check_ARB_copy_buffer(provider, caps, ext);
        this.GL_ARB_copy_image = GLCapabilities.check_ARB_copy_image(provider, caps, ext);
        this.GL_ARB_cull_distance = ext.contains("GL_ARB_cull_distance");
        this.GL_ARB_debug_output = GLCapabilities.check_ARB_debug_output(provider, caps, ext);
        this.GL_ARB_depth_buffer_float = ext.contains("GL_ARB_depth_buffer_float");
        this.GL_ARB_depth_clamp = ext.contains("GL_ARB_depth_clamp");
        this.GL_ARB_depth_texture = ext.contains("GL_ARB_depth_texture");
        this.GL_ARB_derivative_control = ext.contains("GL_ARB_derivative_control");
        this.GL_ARB_direct_state_access = GLCapabilities.check_ARB_direct_state_access(provider, caps, ext);
        this.GL_ARB_draw_buffers = GLCapabilities.check_ARB_draw_buffers(provider, caps, ext);
        this.GL_ARB_draw_buffers_blend = GLCapabilities.check_ARB_draw_buffers_blend(provider, caps, ext);
        this.GL_ARB_draw_elements_base_vertex = GLCapabilities.check_ARB_draw_elements_base_vertex(provider, caps, ext);
        this.GL_ARB_draw_indirect = GLCapabilities.check_ARB_draw_indirect(provider, caps, ext);
        this.GL_ARB_draw_instanced = GLCapabilities.check_ARB_draw_instanced(provider, caps, ext);
        this.GL_ARB_enhanced_layouts = ext.contains("GL_ARB_enhanced_layouts");
        this.GL_ARB_ES2_compatibility = GLCapabilities.check_ARB_ES2_compatibility(provider, caps, ext);
        this.GL_ARB_ES3_1_compatibility = GLCapabilities.check_ARB_ES3_1_compatibility(provider, caps, ext);
        this.GL_ARB_ES3_2_compatibility = GLCapabilities.check_ARB_ES3_2_compatibility(provider, caps, ext);
        this.GL_ARB_ES3_compatibility = ext.contains("GL_ARB_ES3_compatibility");
        this.GL_ARB_explicit_attrib_location = ext.contains("GL_ARB_explicit_attrib_location");
        this.GL_ARB_explicit_uniform_location = ext.contains("GL_ARB_explicit_uniform_location");
        this.GL_ARB_fragment_coord_conventions = ext.contains("GL_ARB_fragment_coord_conventions");
        this.GL_ARB_fragment_layer_viewport = ext.contains("GL_ARB_fragment_layer_viewport");
        this.GL_ARB_fragment_program = ext.contains("GL_ARB_fragment_program");
        this.GL_ARB_fragment_program_shadow = ext.contains("GL_ARB_fragment_program_shadow");
        this.GL_ARB_fragment_shader = ext.contains("GL_ARB_fragment_shader");
        this.GL_ARB_fragment_shader_interlock = ext.contains("GL_ARB_fragment_shader_interlock");
        this.GL_ARB_framebuffer_no_attachments = GLCapabilities.check_ARB_framebuffer_no_attachments(provider, caps, ext);
        this.GL_ARB_framebuffer_object = GLCapabilities.check_ARB_framebuffer_object(provider, caps, ext);
        this.GL_ARB_framebuffer_sRGB = ext.contains("GL_ARB_framebuffer_sRGB");
        this.GL_ARB_geometry_shader4 = GLCapabilities.check_ARB_geometry_shader4(provider, caps, ext);
        this.GL_ARB_get_program_binary = GLCapabilities.check_ARB_get_program_binary(provider, caps, ext);
        this.GL_ARB_get_texture_sub_image = GLCapabilities.check_ARB_get_texture_sub_image(provider, caps, ext);
        this.GL_ARB_gl_spirv = GLCapabilities.check_ARB_gl_spirv(provider, caps, ext);
        this.GL_ARB_gpu_shader5 = ext.contains("GL_ARB_gpu_shader5");
        this.GL_ARB_gpu_shader_fp64 = GLCapabilities.check_ARB_gpu_shader_fp64(provider, caps, ext);
        this.GL_ARB_gpu_shader_int64 = GLCapabilities.check_ARB_gpu_shader_int64(provider, caps, ext);
        this.GL_ARB_half_float_pixel = ext.contains("GL_ARB_half_float_pixel");
        this.GL_ARB_half_float_vertex = ext.contains("GL_ARB_half_float_vertex");
        this.GL_ARB_imaging = GLCapabilities.check_ARB_imaging(provider, caps, ext, fc);
        this.GL_ARB_indirect_parameters = GLCapabilities.check_ARB_indirect_parameters(provider, caps, ext);
        this.GL_ARB_instanced_arrays = GLCapabilities.check_ARB_instanced_arrays(provider, caps, ext);
        this.GL_ARB_internalformat_query = GLCapabilities.check_ARB_internalformat_query(provider, caps, ext);
        this.GL_ARB_internalformat_query2 = GLCapabilities.check_ARB_internalformat_query2(provider, caps, ext);
        this.GL_ARB_invalidate_subdata = GLCapabilities.check_ARB_invalidate_subdata(provider, caps, ext);
        this.GL_ARB_map_buffer_alignment = ext.contains("GL_ARB_map_buffer_alignment");
        this.GL_ARB_map_buffer_range = GLCapabilities.check_ARB_map_buffer_range(provider, caps, ext);
        this.GL_ARB_matrix_palette = GLCapabilities.check_ARB_matrix_palette(provider, caps, ext);
        this.GL_ARB_multi_bind = GLCapabilities.check_ARB_multi_bind(provider, caps, ext);
        this.GL_ARB_multi_draw_indirect = GLCapabilities.check_ARB_multi_draw_indirect(provider, caps, ext);
        this.GL_ARB_multisample = GLCapabilities.check_ARB_multisample(provider, caps, ext);
        this.GL_ARB_multitexture = GLCapabilities.check_ARB_multitexture(provider, caps, ext);
        this.GL_ARB_occlusion_query = GLCapabilities.check_ARB_occlusion_query(provider, caps, ext);
        this.GL_ARB_occlusion_query2 = ext.contains("GL_ARB_occlusion_query2");
        this.GL_ARB_parallel_shader_compile = GLCapabilities.check_ARB_parallel_shader_compile(provider, caps, ext);
        this.GL_ARB_pipeline_statistics_query = ext.contains("GL_ARB_pipeline_statistics_query");
        this.GL_ARB_pixel_buffer_object = ext.contains("GL_ARB_pixel_buffer_object");
        this.GL_ARB_point_parameters = GLCapabilities.check_ARB_point_parameters(provider, caps, ext);
        this.GL_ARB_point_sprite = ext.contains("GL_ARB_point_sprite");
        this.GL_ARB_polygon_offset_clamp = GLCapabilities.check_ARB_polygon_offset_clamp(provider, caps, ext);
        this.GL_ARB_post_depth_coverage = ext.contains("GL_ARB_post_depth_coverage");
        this.GL_ARB_program_interface_query = GLCapabilities.check_ARB_program_interface_query(provider, caps, ext);
        this.GL_ARB_provoking_vertex = GLCapabilities.check_ARB_provoking_vertex(provider, caps, ext);
        this.GL_ARB_query_buffer_object = ext.contains("GL_ARB_query_buffer_object");
        this.GL_ARB_robust_buffer_access_behavior = ext.contains("GL_ARB_robust_buffer_access_behavior");
        this.GL_ARB_robustness = GLCapabilities.check_ARB_robustness(provider, caps, ext);
        this.GL_ARB_robustness_application_isolation = ext.contains("GL_ARB_robustness_application_isolation");
        this.GL_ARB_robustness_share_group_isolation = ext.contains("GL_ARB_robustness_share_group_isolation");
        this.GL_ARB_sample_locations = GLCapabilities.check_ARB_sample_locations(provider, caps, ext);
        this.GL_ARB_sample_shading = GLCapabilities.check_ARB_sample_shading(provider, caps, ext);
        this.GL_ARB_sampler_objects = GLCapabilities.check_ARB_sampler_objects(provider, caps, ext);
        this.GL_ARB_seamless_cube_map = ext.contains("GL_ARB_seamless_cube_map");
        this.GL_ARB_seamless_cubemap_per_texture = ext.contains("GL_ARB_seamless_cubemap_per_texture");
        this.GL_ARB_separate_shader_objects = GLCapabilities.check_ARB_separate_shader_objects(provider, caps, ext);
        this.GL_ARB_shader_atomic_counter_ops = ext.contains("GL_ARB_shader_atomic_counter_ops");
        this.GL_ARB_shader_atomic_counters = GLCapabilities.check_ARB_shader_atomic_counters(provider, caps, ext);
        this.GL_ARB_shader_ballot = ext.contains("GL_ARB_shader_ballot");
        this.GL_ARB_shader_bit_encoding = ext.contains("GL_ARB_shader_bit_encoding");
        this.GL_ARB_shader_clock = ext.contains("GL_ARB_shader_clock");
        this.GL_ARB_shader_draw_parameters = ext.contains("GL_ARB_shader_draw_parameters");
        this.GL_ARB_shader_group_vote = ext.contains("GL_ARB_shader_group_vote");
        this.GL_ARB_shader_image_load_store = GLCapabilities.check_ARB_shader_image_load_store(provider, caps, ext);
        this.GL_ARB_shader_image_size = ext.contains("GL_ARB_shader_image_size");
        this.GL_ARB_shader_objects = GLCapabilities.check_ARB_shader_objects(provider, caps, ext);
        this.GL_ARB_shader_precision = ext.contains("GL_ARB_shader_precision");
        this.GL_ARB_shader_stencil_export = ext.contains("GL_ARB_shader_stencil_export");
        this.GL_ARB_shader_storage_buffer_object = GLCapabilities.check_ARB_shader_storage_buffer_object(provider, caps, ext);
        this.GL_ARB_shader_subroutine = GLCapabilities.check_ARB_shader_subroutine(provider, caps, ext);
        this.GL_ARB_shader_texture_image_samples = ext.contains("GL_ARB_shader_texture_image_samples");
        this.GL_ARB_shader_texture_lod = ext.contains("GL_ARB_shader_texture_lod");
        this.GL_ARB_shader_viewport_layer_array = ext.contains("GL_ARB_shader_viewport_layer_array");
        this.GL_ARB_shading_language_100 = ext.contains("GL_ARB_shading_language_100");
        this.GL_ARB_shading_language_420pack = ext.contains("GL_ARB_shading_language_420pack");
        this.GL_ARB_shading_language_include = GLCapabilities.check_ARB_shading_language_include(provider, caps, ext);
        this.GL_ARB_shading_language_packing = ext.contains("GL_ARB_shading_language_packing");
        this.GL_ARB_shadow = ext.contains("GL_ARB_shadow");
        this.GL_ARB_shadow_ambient = ext.contains("GL_ARB_shadow_ambient");
        this.GL_ARB_sparse_buffer = GLCapabilities.check_ARB_sparse_buffer(provider, caps, ext);
        this.GL_ARB_sparse_texture = GLCapabilities.check_ARB_sparse_texture(provider, caps, ext);
        this.GL_ARB_sparse_texture2 = ext.contains("GL_ARB_sparse_texture2");
        this.GL_ARB_sparse_texture_clamp = ext.contains("GL_ARB_sparse_texture_clamp");
        this.GL_ARB_spirv_extensions = ext.contains("GL_ARB_spirv_extensions");
        this.GL_ARB_stencil_texturing = ext.contains("GL_ARB_stencil_texturing");
        this.GL_ARB_sync = GLCapabilities.check_ARB_sync(provider, caps, ext);
        this.GL_ARB_tessellation_shader = GLCapabilities.check_ARB_tessellation_shader(provider, caps, ext);
        this.GL_ARB_texture_barrier = GLCapabilities.check_ARB_texture_barrier(provider, caps, ext);
        this.GL_ARB_texture_border_clamp = ext.contains("GL_ARB_texture_border_clamp");
        this.GL_ARB_texture_buffer_object = GLCapabilities.check_ARB_texture_buffer_object(provider, caps, ext);
        this.GL_ARB_texture_buffer_object_rgb32 = ext.contains("GL_ARB_texture_buffer_object_rgb32");
        this.GL_ARB_texture_buffer_range = GLCapabilities.check_ARB_texture_buffer_range(provider, caps, ext);
        this.GL_ARB_texture_compression = GLCapabilities.check_ARB_texture_compression(provider, caps, ext);
        this.GL_ARB_texture_compression_bptc = ext.contains("GL_ARB_texture_compression_bptc");
        this.GL_ARB_texture_compression_rgtc = ext.contains("GL_ARB_texture_compression_rgtc");
        this.GL_ARB_texture_cube_map = ext.contains("GL_ARB_texture_cube_map");
        this.GL_ARB_texture_cube_map_array = ext.contains("GL_ARB_texture_cube_map_array");
        this.GL_ARB_texture_env_add = ext.contains("GL_ARB_texture_env_add");
        this.GL_ARB_texture_env_combine = ext.contains("GL_ARB_texture_env_combine");
        this.GL_ARB_texture_env_crossbar = ext.contains("GL_ARB_texture_env_crossbar");
        this.GL_ARB_texture_env_dot3 = ext.contains("GL_ARB_texture_env_dot3");
        this.GL_ARB_texture_filter_anisotropic = ext.contains("GL_ARB_texture_filter_anisotropic");
        this.GL_ARB_texture_filter_minmax = ext.contains("GL_ARB_texture_filter_minmax");
        this.GL_ARB_texture_float = ext.contains("GL_ARB_texture_float");
        this.GL_ARB_texture_gather = ext.contains("GL_ARB_texture_gather");
        this.GL_ARB_texture_mirror_clamp_to_edge = ext.contains("GL_ARB_texture_mirror_clamp_to_edge");
        this.GL_ARB_texture_mirrored_repeat = ext.contains("GL_ARB_texture_mirrored_repeat");
        this.GL_ARB_texture_multisample = GLCapabilities.check_ARB_texture_multisample(provider, caps, ext);
        this.GL_ARB_texture_non_power_of_two = ext.contains("GL_ARB_texture_non_power_of_two");
        this.GL_ARB_texture_query_levels = ext.contains("GL_ARB_texture_query_levels");
        this.GL_ARB_texture_query_lod = ext.contains("GL_ARB_texture_query_lod");
        this.GL_ARB_texture_rectangle = ext.contains("GL_ARB_texture_rectangle");
        this.GL_ARB_texture_rg = ext.contains("GL_ARB_texture_rg");
        this.GL_ARB_texture_rgb10_a2ui = ext.contains("GL_ARB_texture_rgb10_a2ui");
        this.GL_ARB_texture_stencil8 = ext.contains("GL_ARB_texture_stencil8");
        this.GL_ARB_texture_storage = GLCapabilities.check_ARB_texture_storage(provider, caps, ext);
        this.GL_ARB_texture_storage_multisample = GLCapabilities.check_ARB_texture_storage_multisample(provider, caps, ext);
        this.GL_ARB_texture_swizzle = ext.contains("GL_ARB_texture_swizzle");
        this.GL_ARB_texture_view = GLCapabilities.check_ARB_texture_view(provider, caps, ext);
        this.GL_ARB_timer_query = GLCapabilities.check_ARB_timer_query(provider, caps, ext);
        this.GL_ARB_transform_feedback2 = GLCapabilities.check_ARB_transform_feedback2(provider, caps, ext);
        this.GL_ARB_transform_feedback3 = GLCapabilities.check_ARB_transform_feedback3(provider, caps, ext);
        this.GL_ARB_transform_feedback_instanced = GLCapabilities.check_ARB_transform_feedback_instanced(provider, caps, ext);
        this.GL_ARB_transform_feedback_overflow_query = ext.contains("GL_ARB_transform_feedback_overflow_query");
        this.GL_ARB_transpose_matrix = GLCapabilities.check_ARB_transpose_matrix(provider, caps, ext);
        this.GL_ARB_uniform_buffer_object = GLCapabilities.check_ARB_uniform_buffer_object(provider, caps, ext);
        this.GL_ARB_vertex_array_bgra = ext.contains("GL_ARB_vertex_array_bgra");
        this.GL_ARB_vertex_array_object = GLCapabilities.check_ARB_vertex_array_object(provider, caps, ext);
        this.GL_ARB_vertex_attrib_64bit = GLCapabilities.check_ARB_vertex_attrib_64bit(provider, caps, ext);
        this.GL_ARB_vertex_attrib_binding = GLCapabilities.check_ARB_vertex_attrib_binding(provider, caps, ext);
        this.GL_ARB_vertex_blend = GLCapabilities.check_ARB_vertex_blend(provider, caps, ext);
        this.GL_ARB_vertex_buffer_object = GLCapabilities.check_ARB_vertex_buffer_object(provider, caps, ext);
        this.GL_ARB_vertex_program = GLCapabilities.check_ARB_vertex_program(provider, caps, ext);
        this.GL_ARB_vertex_shader = GLCapabilities.check_ARB_vertex_shader(provider, caps, ext);
        this.GL_ARB_vertex_type_10f_11f_11f_rev = ext.contains("GL_ARB_vertex_type_10f_11f_11f_rev");
        this.GL_ARB_vertex_type_2_10_10_10_rev = GLCapabilities.check_ARB_vertex_type_2_10_10_10_rev(provider, caps, ext, fc);
        this.GL_ARB_viewport_array = GLCapabilities.check_ARB_viewport_array(provider, caps, ext);
        this.GL_ARB_window_pos = GLCapabilities.check_ARB_window_pos(provider, caps, ext);
        this.GL_ATI_meminfo = ext.contains("GL_ATI_meminfo");
        this.GL_ATI_shader_texture_lod = ext.contains("GL_ATI_shader_texture_lod");
        this.GL_ATI_texture_compression_3dc = ext.contains("GL_ATI_texture_compression_3dc");
        this.GL_EXT_422_pixels = ext.contains("GL_EXT_422_pixels");
        this.GL_EXT_abgr = ext.contains("GL_EXT_abgr");
        this.GL_EXT_bgra = ext.contains("GL_EXT_bgra");
        this.GL_EXT_bindable_uniform = GLCapabilities.check_EXT_bindable_uniform(provider, caps, ext);
        this.GL_EXT_blend_color = GLCapabilities.check_EXT_blend_color(provider, caps, ext);
        this.GL_EXT_blend_equation_separate = GLCapabilities.check_EXT_blend_equation_separate(provider, caps, ext);
        this.GL_EXT_blend_func_separate = GLCapabilities.check_EXT_blend_func_separate(provider, caps, ext);
        this.GL_EXT_blend_minmax = GLCapabilities.check_EXT_blend_minmax(provider, caps, ext);
        this.GL_EXT_blend_subtract = ext.contains("GL_EXT_blend_subtract");
        this.GL_EXT_clip_volume_hint = ext.contains("GL_EXT_clip_volume_hint");
        this.GL_EXT_compiled_vertex_array = GLCapabilities.check_EXT_compiled_vertex_array(provider, caps, ext);
        this.GL_EXT_debug_label = GLCapabilities.check_EXT_debug_label(provider, caps, ext);
        this.GL_EXT_debug_marker = GLCapabilities.check_EXT_debug_marker(provider, caps, ext);
        this.GL_EXT_depth_bounds_test = GLCapabilities.check_EXT_depth_bounds_test(provider, caps, ext);
        this.GL_EXT_direct_state_access = GLCapabilities.check_EXT_direct_state_access(provider, caps, ext);
        this.GL_EXT_draw_buffers2 = GLCapabilities.check_EXT_draw_buffers2(provider, caps, ext);
        this.GL_EXT_draw_instanced = GLCapabilities.check_EXT_draw_instanced(provider, caps, ext);
        this.GL_EXT_EGL_image_storage = GLCapabilities.check_EXT_EGL_image_storage(provider, caps, ext);
        this.GL_EXT_EGL_sync = ext.contains("GL_EXT_EGL_sync");
        this.GL_EXT_external_buffer = GLCapabilities.check_EXT_external_buffer(provider, caps, ext);
        this.GL_EXT_framebuffer_blit = GLCapabilities.check_EXT_framebuffer_blit(provider, caps, ext);
        this.GL_EXT_framebuffer_blit_layers = GLCapabilities.check_EXT_framebuffer_blit_layers(provider, caps, ext);
        this.GL_EXT_framebuffer_multisample = GLCapabilities.check_EXT_framebuffer_multisample(provider, caps, ext);
        this.GL_EXT_framebuffer_multisample_blit_scaled = ext.contains("GL_EXT_framebuffer_multisample_blit_scaled");
        this.GL_EXT_framebuffer_object = GLCapabilities.check_EXT_framebuffer_object(provider, caps, ext);
        this.GL_EXT_framebuffer_sRGB = ext.contains("GL_EXT_framebuffer_sRGB");
        this.GL_EXT_geometry_shader4 = GLCapabilities.check_EXT_geometry_shader4(provider, caps, ext);
        this.GL_EXT_gpu_program_parameters = GLCapabilities.check_EXT_gpu_program_parameters(provider, caps, ext);
        this.GL_EXT_gpu_shader4 = GLCapabilities.check_EXT_gpu_shader4(provider, caps, ext);
        this.GL_EXT_memory_object = GLCapabilities.check_EXT_memory_object(provider, caps, ext);
        this.GL_EXT_memory_object_fd = GLCapabilities.check_EXT_memory_object_fd(provider, caps, ext);
        this.GL_EXT_memory_object_win32 = GLCapabilities.check_EXT_memory_object_win32(provider, caps, ext);
        this.GL_EXT_multiview_tessellation_geometry_shader = ext.contains("GL_EXT_multiview_tessellation_geometry_shader");
        this.GL_EXT_multiview_texture_multisample = ext.contains("GL_EXT_multiview_texture_multisample");
        this.GL_EXT_multiview_timer_query = ext.contains("GL_EXT_multiview_timer_query");
        this.GL_EXT_packed_depth_stencil = ext.contains("GL_EXT_packed_depth_stencil");
        this.GL_EXT_packed_float = ext.contains("GL_EXT_packed_float");
        this.GL_EXT_pixel_buffer_object = ext.contains("GL_EXT_pixel_buffer_object");
        this.GL_EXT_point_parameters = GLCapabilities.check_EXT_point_parameters(provider, caps, ext);
        this.GL_EXT_polygon_offset_clamp = GLCapabilities.check_EXT_polygon_offset_clamp(provider, caps, ext);
        this.GL_EXT_post_depth_coverage = ext.contains("GL_EXT_post_depth_coverage");
        this.GL_EXT_provoking_vertex = GLCapabilities.check_EXT_provoking_vertex(provider, caps, ext);
        this.GL_EXT_raster_multisample = GLCapabilities.check_EXT_raster_multisample(provider, caps, ext);
        this.GL_EXT_secondary_color = GLCapabilities.check_EXT_secondary_color(provider, caps, ext);
        this.GL_EXT_semaphore = GLCapabilities.check_EXT_semaphore(provider, caps, ext);
        this.GL_EXT_semaphore_fd = GLCapabilities.check_EXT_semaphore_fd(provider, caps, ext);
        this.GL_EXT_semaphore_win32 = GLCapabilities.check_EXT_semaphore_win32(provider, caps, ext);
        this.GL_EXT_separate_shader_objects = GLCapabilities.check_EXT_separate_shader_objects(provider, caps, ext);
        this.GL_EXT_shader_framebuffer_fetch = ext.contains("GL_EXT_shader_framebuffer_fetch");
        this.GL_EXT_shader_framebuffer_fetch_non_coherent = GLCapabilities.check_EXT_shader_framebuffer_fetch_non_coherent(provider, caps, ext);
        this.GL_EXT_shader_image_load_formatted = ext.contains("GL_EXT_shader_image_load_formatted");
        this.GL_EXT_shader_image_load_store = GLCapabilities.check_EXT_shader_image_load_store(provider, caps, ext);
        this.GL_EXT_shader_integer_mix = ext.contains("GL_EXT_shader_integer_mix");
        this.GL_EXT_shader_samples_identical = ext.contains("GL_EXT_shader_samples_identical");
        this.GL_EXT_shadow_funcs = ext.contains("GL_EXT_shadow_funcs");
        this.GL_EXT_shared_texture_palette = ext.contains("GL_EXT_shared_texture_palette");
        this.GL_EXT_sparse_texture2 = ext.contains("GL_EXT_sparse_texture2");
        this.GL_EXT_stencil_clear_tag = GLCapabilities.check_EXT_stencil_clear_tag(provider, caps, ext);
        this.GL_EXT_stencil_two_side = GLCapabilities.check_EXT_stencil_two_side(provider, caps, ext);
        this.GL_EXT_stencil_wrap = ext.contains("GL_EXT_stencil_wrap");
        this.GL_EXT_texture_array = GLCapabilities.check_EXT_texture_array(provider, caps, ext);
        this.GL_EXT_texture_buffer_object = GLCapabilities.check_EXT_texture_buffer_object(provider, caps, ext);
        this.GL_EXT_texture_compression_latc = ext.contains("GL_EXT_texture_compression_latc");
        this.GL_EXT_texture_compression_rgtc = ext.contains("GL_EXT_texture_compression_rgtc");
        this.GL_EXT_texture_compression_s3tc = ext.contains("GL_EXT_texture_compression_s3tc");
        this.GL_EXT_texture_filter_anisotropic = ext.contains("GL_EXT_texture_filter_anisotropic");
        this.GL_EXT_texture_filter_minmax = ext.contains("GL_EXT_texture_filter_minmax");
        this.GL_EXT_texture_integer = GLCapabilities.check_EXT_texture_integer(provider, caps, ext);
        this.GL_EXT_texture_mirror_clamp = ext.contains("GL_EXT_texture_mirror_clamp");
        this.GL_EXT_texture_shadow_lod = ext.contains("GL_EXT_texture_shadow_lod");
        this.GL_EXT_texture_shared_exponent = ext.contains("GL_EXT_texture_shared_exponent");
        this.GL_EXT_texture_snorm = ext.contains("GL_EXT_texture_snorm");
        this.GL_EXT_texture_sRGB = ext.contains("GL_EXT_texture_sRGB");
        this.GL_EXT_texture_sRGB_decode = ext.contains("GL_EXT_texture_sRGB_decode");
        this.GL_EXT_texture_sRGB_R8 = ext.contains("GL_EXT_texture_sRGB_R8");
        this.GL_EXT_texture_sRGB_RG8 = ext.contains("GL_EXT_texture_sRGB_RG8");
        this.GL_EXT_texture_storage = GLCapabilities.check_EXT_texture_storage(provider, caps, ext);
        this.GL_EXT_texture_swizzle = ext.contains("GL_EXT_texture_swizzle");
        this.GL_EXT_timer_query = GLCapabilities.check_EXT_timer_query(provider, caps, ext);
        this.GL_EXT_transform_feedback = GLCapabilities.check_EXT_transform_feedback(provider, caps, ext);
        this.GL_EXT_vertex_array_bgra = ext.contains("GL_EXT_vertex_array_bgra");
        this.GL_EXT_vertex_attrib_64bit = GLCapabilities.check_EXT_vertex_attrib_64bit(provider, caps, ext);
        this.GL_EXT_win32_keyed_mutex = GLCapabilities.check_EXT_win32_keyed_mutex(provider, caps, ext);
        this.GL_EXT_window_rectangles = GLCapabilities.check_EXT_window_rectangles(provider, caps, ext);
        this.GL_EXT_x11_sync_object = GLCapabilities.check_EXT_x11_sync_object(provider, caps, ext);
        this.GL_GREMEDY_frame_terminator = GLCapabilities.check_GREMEDY_frame_terminator(provider, caps, ext);
        this.GL_GREMEDY_string_marker = GLCapabilities.check_GREMEDY_string_marker(provider, caps, ext);
        this.GL_INTEL_blackhole_render = ext.contains("GL_INTEL_blackhole_render");
        this.GL_INTEL_conservative_rasterization = ext.contains("GL_INTEL_conservative_rasterization");
        this.GL_INTEL_fragment_shader_ordering = ext.contains("GL_INTEL_fragment_shader_ordering");
        this.GL_INTEL_framebuffer_CMAA = GLCapabilities.check_INTEL_framebuffer_CMAA(provider, caps, ext);
        this.GL_INTEL_map_texture = GLCapabilities.check_INTEL_map_texture(provider, caps, ext);
        this.GL_INTEL_performance_query = GLCapabilities.check_INTEL_performance_query(provider, caps, ext);
        this.GL_INTEL_shader_integer_functions2 = ext.contains("GL_INTEL_shader_integer_functions2");
        this.GL_KHR_blend_equation_advanced = GLCapabilities.check_KHR_blend_equation_advanced(provider, caps, ext);
        this.GL_KHR_blend_equation_advanced_coherent = ext.contains("GL_KHR_blend_equation_advanced_coherent");
        this.GL_KHR_context_flush_control = ext.contains("GL_KHR_context_flush_control");
        this.GL_KHR_debug = GLCapabilities.check_KHR_debug(provider, caps, ext);
        this.GL_KHR_no_error = ext.contains("GL_KHR_no_error");
        this.GL_KHR_parallel_shader_compile = GLCapabilities.check_KHR_parallel_shader_compile(provider, caps, ext);
        this.GL_KHR_robust_buffer_access_behavior = ext.contains("GL_KHR_robust_buffer_access_behavior");
        this.GL_KHR_robustness = GLCapabilities.check_KHR_robustness(provider, caps, ext);
        this.GL_KHR_shader_subgroup = ext.contains("GL_KHR_shader_subgroup");
        this.GL_KHR_texture_compression_astc_hdr = ext.contains("GL_KHR_texture_compression_astc_hdr");
        this.GL_KHR_texture_compression_astc_ldr = ext.contains("GL_KHR_texture_compression_astc_ldr");
        this.GL_KHR_texture_compression_astc_sliced_3d = ext.contains("GL_KHR_texture_compression_astc_sliced_3d");
        this.GL_MESA_framebuffer_flip_x = ext.contains("GL_MESA_framebuffer_flip_x");
        this.GL_MESA_framebuffer_flip_y = GLCapabilities.check_MESA_framebuffer_flip_y(provider, caps, ext);
        this.GL_MESA_framebuffer_swap_xy = ext.contains("GL_MESA_framebuffer_swap_xy");
        this.GL_MESA_tile_raster_order = ext.contains("GL_MESA_tile_raster_order");
        this.GL_NV_alpha_to_coverage_dither_control = GLCapabilities.check_NV_alpha_to_coverage_dither_control(provider, caps, ext);
        this.GL_NV_bindless_multi_draw_indirect = GLCapabilities.check_NV_bindless_multi_draw_indirect(provider, caps, ext);
        this.GL_NV_bindless_multi_draw_indirect_count = GLCapabilities.check_NV_bindless_multi_draw_indirect_count(provider, caps, ext);
        this.GL_NV_bindless_texture = GLCapabilities.check_NV_bindless_texture(provider, caps, ext);
        this.GL_NV_blend_equation_advanced = GLCapabilities.check_NV_blend_equation_advanced(provider, caps, ext);
        this.GL_NV_blend_equation_advanced_coherent = ext.contains("GL_NV_blend_equation_advanced_coherent");
        this.GL_NV_blend_minmax_factor = ext.contains("GL_NV_blend_minmax_factor");
        this.GL_NV_blend_square = ext.contains("GL_NV_blend_square");
        this.GL_NV_clip_space_w_scaling = GLCapabilities.check_NV_clip_space_w_scaling(provider, caps, ext);
        this.GL_NV_command_list = GLCapabilities.check_NV_command_list(provider, caps, ext);
        this.GL_NV_compute_shader_derivatives = ext.contains("GL_NV_compute_shader_derivatives");
        this.GL_NV_conditional_render = GLCapabilities.check_NV_conditional_render(provider, caps, ext);
        this.GL_NV_conservative_raster = GLCapabilities.check_NV_conservative_raster(provider, caps, ext);
        this.GL_NV_conservative_raster_dilate = GLCapabilities.check_NV_conservative_raster_dilate(provider, caps, ext);
        this.GL_NV_conservative_raster_pre_snap = ext.contains("GL_NV_conservative_raster_pre_snap");
        this.GL_NV_conservative_raster_pre_snap_triangles = GLCapabilities.check_NV_conservative_raster_pre_snap_triangles(provider, caps, ext);
        this.GL_NV_conservative_raster_underestimation = ext.contains("GL_NV_conservative_raster_underestimation");
        this.GL_NV_copy_depth_to_color = ext.contains("GL_NV_copy_depth_to_color");
        this.GL_NV_copy_image = GLCapabilities.check_NV_copy_image(provider, caps, ext);
        this.GL_NV_deep_texture3D = ext.contains("GL_NV_deep_texture3D");
        this.GL_NV_depth_buffer_float = GLCapabilities.check_NV_depth_buffer_float(provider, caps, ext);
        this.GL_NV_depth_clamp = ext.contains("GL_NV_depth_clamp");
        this.GL_NV_draw_texture = GLCapabilities.check_NV_draw_texture(provider, caps, ext);
        this.GL_NV_draw_vulkan_image = GLCapabilities.check_NV_draw_vulkan_image(provider, caps, ext);
        this.GL_NV_ES3_1_compatibility = ext.contains("GL_NV_ES3_1_compatibility");
        this.GL_NV_explicit_multisample = GLCapabilities.check_NV_explicit_multisample(provider, caps, ext);
        this.GL_NV_fence = GLCapabilities.check_NV_fence(provider, caps, ext);
        this.GL_NV_fill_rectangle = ext.contains("GL_NV_fill_rectangle");
        this.GL_NV_float_buffer = ext.contains("GL_NV_float_buffer");
        this.GL_NV_fog_distance = ext.contains("GL_NV_fog_distance");
        this.GL_NV_fragment_coverage_to_color = GLCapabilities.check_NV_fragment_coverage_to_color(provider, caps, ext);
        this.GL_NV_fragment_program4 = ext.contains("GL_NV_fragment_program4");
        this.GL_NV_fragment_program_option = ext.contains("GL_NV_fragment_program_option");
        this.GL_NV_fragment_shader_barycentric = ext.contains("GL_NV_fragment_shader_barycentric");
        this.GL_NV_fragment_shader_interlock = ext.contains("GL_NV_fragment_shader_interlock");
        this.GL_NV_framebuffer_mixed_samples = GLCapabilities.check_NV_framebuffer_mixed_samples(provider, caps, ext);
        this.GL_NV_framebuffer_multisample_coverage = GLCapabilities.check_NV_framebuffer_multisample_coverage(provider, caps, ext);
        this.GL_NV_geometry_shader4 = ext.contains("GL_NV_geometry_shader4");
        this.GL_NV_geometry_shader_passthrough = ext.contains("GL_NV_geometry_shader_passthrough");
        this.GL_NV_gpu_multicast = GLCapabilities.check_NV_gpu_multicast(provider, caps, ext);
        this.GL_NV_gpu_shader5 = GLCapabilities.check_NV_gpu_shader5(provider, caps, ext);
        this.GL_NV_half_float = GLCapabilities.check_NV_half_float(provider, caps, ext);
        this.GL_NV_internalformat_sample_query = GLCapabilities.check_NV_internalformat_sample_query(provider, caps, ext);
        this.GL_NV_light_max_exponent = ext.contains("GL_NV_light_max_exponent");
        this.GL_NV_memory_attachment = GLCapabilities.check_NV_memory_attachment(provider, caps, ext);
        this.GL_NV_memory_object_sparse = GLCapabilities.check_NV_memory_object_sparse(provider, caps, ext);
        this.GL_NV_mesh_shader = GLCapabilities.check_NV_mesh_shader(provider, caps, ext);
        this.GL_NV_multisample_coverage = ext.contains("GL_NV_multisample_coverage");
        this.GL_NV_multisample_filter_hint = ext.contains("GL_NV_multisample_filter_hint");
        this.GL_NV_packed_depth_stencil = ext.contains("GL_NV_packed_depth_stencil");
        this.GL_NV_path_rendering = GLCapabilities.check_NV_path_rendering(provider, caps, ext);
        this.GL_NV_path_rendering_shared_edge = ext.contains("GL_NV_path_rendering_shared_edge");
        this.GL_NV_pixel_data_range = GLCapabilities.check_NV_pixel_data_range(provider, caps, ext);
        this.GL_NV_point_sprite = GLCapabilities.check_NV_point_sprite(provider, caps, ext);
        this.GL_NV_primitive_restart = GLCapabilities.check_NV_primitive_restart(provider, caps, ext);
        this.GL_NV_primitive_shading_rate = ext.contains("GL_NV_primitive_shading_rate");
        this.GL_NV_query_resource = GLCapabilities.check_NV_query_resource(provider, caps, ext);
        this.GL_NV_query_resource_tag = GLCapabilities.check_NV_query_resource_tag(provider, caps, ext);
        this.GL_NV_representative_fragment_test = ext.contains("GL_NV_representative_fragment_test");
        this.GL_NV_robustness_video_memory_purge = ext.contains("GL_NV_robustness_video_memory_purge");
        this.GL_NV_sample_locations = GLCapabilities.check_NV_sample_locations(provider, caps, ext);
        this.GL_NV_sample_mask_override_coverage = ext.contains("GL_NV_sample_mask_override_coverage");
        this.GL_NV_scissor_exclusive = GLCapabilities.check_NV_scissor_exclusive(provider, caps, ext);
        this.GL_NV_shader_atomic_float = ext.contains("GL_NV_shader_atomic_float");
        this.GL_NV_shader_atomic_float64 = ext.contains("GL_NV_shader_atomic_float64");
        this.GL_NV_shader_atomic_fp16_vector = ext.contains("GL_NV_shader_atomic_fp16_vector");
        this.GL_NV_shader_atomic_int64 = ext.contains("GL_NV_shader_atomic_int64");
        this.GL_NV_shader_buffer_load = GLCapabilities.check_NV_shader_buffer_load(provider, caps, ext);
        this.GL_NV_shader_buffer_store = ext.contains("GL_NV_shader_buffer_store");
        this.GL_NV_shader_subgroup_partitioned = ext.contains("GL_NV_shader_subgroup_partitioned");
        this.GL_NV_shader_texture_footprint = ext.contains("GL_NV_shader_texture_footprint");
        this.GL_NV_shader_thread_group = ext.contains("GL_NV_shader_thread_group");
        this.GL_NV_shader_thread_shuffle = ext.contains("GL_NV_shader_thread_shuffle");
        this.GL_NV_shading_rate_image = GLCapabilities.check_NV_shading_rate_image(provider, caps, ext);
        this.GL_NV_stereo_view_rendering = ext.contains("GL_NV_stereo_view_rendering");
        this.GL_NV_texgen_reflection = ext.contains("GL_NV_texgen_reflection");
        this.GL_NV_texture_barrier = GLCapabilities.check_NV_texture_barrier(provider, caps, ext);
        this.GL_NV_texture_compression_vtc = ext.contains("GL_NV_texture_compression_vtc");
        this.GL_NV_texture_multisample = GLCapabilities.check_NV_texture_multisample(provider, caps, ext);
        this.GL_NV_texture_rectangle_compressed = ext.contains("GL_NV_texture_rectangle_compressed");
        this.GL_NV_texture_shader = ext.contains("GL_NV_texture_shader");
        this.GL_NV_texture_shader2 = ext.contains("GL_NV_texture_shader2");
        this.GL_NV_texture_shader3 = ext.contains("GL_NV_texture_shader3");
        this.GL_NV_timeline_semaphore = GLCapabilities.check_NV_timeline_semaphore(provider, caps, ext);
        this.GL_NV_transform_feedback = GLCapabilities.check_NV_transform_feedback(provider, caps, ext);
        this.GL_NV_transform_feedback2 = GLCapabilities.check_NV_transform_feedback2(provider, caps, ext);
        this.GL_NV_uniform_buffer_std430_layout = ext.contains("GL_NV_uniform_buffer_std430_layout");
        this.GL_NV_uniform_buffer_unified_memory = ext.contains("GL_NV_uniform_buffer_unified_memory");
        this.GL_NV_vertex_array_range = GLCapabilities.check_NV_vertex_array_range(provider, caps, ext);
        this.GL_NV_vertex_array_range2 = ext.contains("GL_NV_vertex_array_range2");
        this.GL_NV_vertex_attrib_integer_64bit = GLCapabilities.check_NV_vertex_attrib_integer_64bit(provider, caps, ext);
        this.GL_NV_vertex_buffer_unified_memory = GLCapabilities.check_NV_vertex_buffer_unified_memory(provider, caps, ext);
        this.GL_NV_viewport_array2 = ext.contains("GL_NV_viewport_array2");
        this.GL_NV_viewport_swizzle = GLCapabilities.check_NV_viewport_swizzle(provider, caps, ext);
        this.GL_NVX_blend_equation_advanced_multi_draw_buffers = ext.contains("GL_NVX_blend_equation_advanced_multi_draw_buffers");
        this.GL_NVX_conditional_render = GLCapabilities.check_NVX_conditional_render(provider, caps, ext);
        this.GL_NVX_gpu_memory_info = ext.contains("GL_NVX_gpu_memory_info");
        this.GL_NVX_gpu_multicast2 = GLCapabilities.check_NVX_gpu_multicast2(provider, caps, ext);
        this.GL_NVX_progress_fence = GLCapabilities.check_NVX_progress_fence(provider, caps, ext);
        this.GL_OVR_multiview = GLCapabilities.check_OVR_multiview(provider, caps, ext);
        this.GL_OVR_multiview2 = ext.contains("GL_OVR_multiview2");
        this.GL_S3_s3tc = ext.contains("GL_S3_s3tc");
        this.glEnable = caps.get(0);
        this.glDisable = caps.get(1);
        this.glAccum = caps.get(2);
        this.glAlphaFunc = caps.get(3);
        this.glAreTexturesResident = caps.get(4);
        this.glArrayElement = caps.get(5);
        this.glBegin = caps.get(6);
        this.glBindTexture = caps.get(7);
        this.glBitmap = caps.get(8);
        this.glBlendFunc = caps.get(9);
        this.glCallList = caps.get(10);
        this.glCallLists = caps.get(11);
        this.glClear = caps.get(12);
        this.glClearAccum = caps.get(13);
        this.glClearColor = caps.get(14);
        this.glClearDepth = caps.get(15);
        this.glClearIndex = caps.get(16);
        this.glClearStencil = caps.get(17);
        this.glClipPlane = caps.get(18);
        this.glColor3b = caps.get(19);
        this.glColor3s = caps.get(20);
        this.glColor3i = caps.get(21);
        this.glColor3f = caps.get(22);
        this.glColor3d = caps.get(23);
        this.glColor3ub = caps.get(24);
        this.glColor3us = caps.get(25);
        this.glColor3ui = caps.get(26);
        this.glColor3bv = caps.get(27);
        this.glColor3sv = caps.get(28);
        this.glColor3iv = caps.get(29);
        this.glColor3fv = caps.get(30);
        this.glColor3dv = caps.get(31);
        this.glColor3ubv = caps.get(32);
        this.glColor3usv = caps.get(33);
        this.glColor3uiv = caps.get(34);
        this.glColor4b = caps.get(35);
        this.glColor4s = caps.get(36);
        this.glColor4i = caps.get(37);
        this.glColor4f = caps.get(38);
        this.glColor4d = caps.get(39);
        this.glColor4ub = caps.get(40);
        this.glColor4us = caps.get(41);
        this.glColor4ui = caps.get(42);
        this.glColor4bv = caps.get(43);
        this.glColor4sv = caps.get(44);
        this.glColor4iv = caps.get(45);
        this.glColor4fv = caps.get(46);
        this.glColor4dv = caps.get(47);
        this.glColor4ubv = caps.get(48);
        this.glColor4usv = caps.get(49);
        this.glColor4uiv = caps.get(50);
        this.glColorMask = caps.get(51);
        this.glColorMaterial = caps.get(52);
        this.glColorPointer = caps.get(53);
        this.glCopyPixels = caps.get(54);
        this.glCullFace = caps.get(55);
        this.glDeleteLists = caps.get(56);
        this.glDepthFunc = caps.get(57);
        this.glDepthMask = caps.get(58);
        this.glDepthRange = caps.get(59);
        this.glDisableClientState = caps.get(60);
        this.glDrawArrays = caps.get(61);
        this.glDrawBuffer = caps.get(62);
        this.glDrawElements = caps.get(63);
        this.glDrawPixels = caps.get(64);
        this.glEdgeFlag = caps.get(65);
        this.glEdgeFlagv = caps.get(66);
        this.glEdgeFlagPointer = caps.get(67);
        this.glEnableClientState = caps.get(68);
        this.glEnd = caps.get(69);
        this.glEvalCoord1f = caps.get(70);
        this.glEvalCoord1fv = caps.get(71);
        this.glEvalCoord1d = caps.get(72);
        this.glEvalCoord1dv = caps.get(73);
        this.glEvalCoord2f = caps.get(74);
        this.glEvalCoord2fv = caps.get(75);
        this.glEvalCoord2d = caps.get(76);
        this.glEvalCoord2dv = caps.get(77);
        this.glEvalMesh1 = caps.get(78);
        this.glEvalMesh2 = caps.get(79);
        this.glEvalPoint1 = caps.get(80);
        this.glEvalPoint2 = caps.get(81);
        this.glFeedbackBuffer = caps.get(82);
        this.glFinish = caps.get(83);
        this.glFlush = caps.get(84);
        this.glFogi = caps.get(85);
        this.glFogiv = caps.get(86);
        this.glFogf = caps.get(87);
        this.glFogfv = caps.get(88);
        this.glFrontFace = caps.get(89);
        this.glGenLists = caps.get(90);
        this.glGenTextures = caps.get(91);
        this.glDeleteTextures = caps.get(92);
        this.glGetClipPlane = caps.get(93);
        this.glGetBooleanv = caps.get(94);
        this.glGetFloatv = caps.get(95);
        this.glGetIntegerv = caps.get(96);
        this.glGetDoublev = caps.get(97);
        this.glGetError = caps.get(98);
        this.glGetLightiv = caps.get(99);
        this.glGetLightfv = caps.get(100);
        this.glGetMapiv = caps.get(101);
        this.glGetMapfv = caps.get(102);
        this.glGetMapdv = caps.get(103);
        this.glGetMaterialiv = caps.get(104);
        this.glGetMaterialfv = caps.get(105);
        this.glGetPixelMapfv = caps.get(106);
        this.glGetPixelMapusv = caps.get(107);
        this.glGetPixelMapuiv = caps.get(108);
        this.glGetPointerv = caps.get(109);
        this.glGetPolygonStipple = caps.get(110);
        this.glGetString = caps.get(111);
        this.glGetTexEnviv = caps.get(112);
        this.glGetTexEnvfv = caps.get(113);
        this.glGetTexGeniv = caps.get(114);
        this.glGetTexGenfv = caps.get(115);
        this.glGetTexGendv = caps.get(116);
        this.glGetTexImage = caps.get(117);
        this.glGetTexLevelParameteriv = caps.get(118);
        this.glGetTexLevelParameterfv = caps.get(119);
        this.glGetTexParameteriv = caps.get(120);
        this.glGetTexParameterfv = caps.get(121);
        this.glHint = caps.get(122);
        this.glIndexi = caps.get(123);
        this.glIndexub = caps.get(124);
        this.glIndexs = caps.get(125);
        this.glIndexf = caps.get(126);
        this.glIndexd = caps.get(127);
        this.glIndexiv = caps.get(128);
        this.glIndexubv = caps.get(129);
        this.glIndexsv = caps.get(130);
        this.glIndexfv = caps.get(131);
        this.glIndexdv = caps.get(132);
        this.glIndexMask = caps.get(133);
        this.glIndexPointer = caps.get(134);
        this.glInitNames = caps.get(135);
        this.glInterleavedArrays = caps.get(136);
        this.glIsEnabled = caps.get(137);
        this.glIsList = caps.get(138);
        this.glIsTexture = caps.get(139);
        this.glLightModeli = caps.get(140);
        this.glLightModelf = caps.get(141);
        this.glLightModeliv = caps.get(142);
        this.glLightModelfv = caps.get(143);
        this.glLighti = caps.get(144);
        this.glLightf = caps.get(145);
        this.glLightiv = caps.get(146);
        this.glLightfv = caps.get(147);
        this.glLineStipple = caps.get(148);
        this.glLineWidth = caps.get(149);
        this.glListBase = caps.get(150);
        this.glLoadMatrixf = caps.get(151);
        this.glLoadMatrixd = caps.get(152);
        this.glLoadIdentity = caps.get(153);
        this.glLoadName = caps.get(154);
        this.glLogicOp = caps.get(155);
        this.glMap1f = caps.get(156);
        this.glMap1d = caps.get(157);
        this.glMap2f = caps.get(158);
        this.glMap2d = caps.get(159);
        this.glMapGrid1f = caps.get(160);
        this.glMapGrid1d = caps.get(161);
        this.glMapGrid2f = caps.get(162);
        this.glMapGrid2d = caps.get(163);
        this.glMateriali = caps.get(164);
        this.glMaterialf = caps.get(165);
        this.glMaterialiv = caps.get(166);
        this.glMaterialfv = caps.get(167);
        this.glMatrixMode = caps.get(168);
        this.glMultMatrixf = caps.get(169);
        this.glMultMatrixd = caps.get(170);
        this.glFrustum = caps.get(171);
        this.glNewList = caps.get(172);
        this.glEndList = caps.get(173);
        this.glNormal3f = caps.get(174);
        this.glNormal3b = caps.get(175);
        this.glNormal3s = caps.get(176);
        this.glNormal3i = caps.get(177);
        this.glNormal3d = caps.get(178);
        this.glNormal3fv = caps.get(179);
        this.glNormal3bv = caps.get(180);
        this.glNormal3sv = caps.get(181);
        this.glNormal3iv = caps.get(182);
        this.glNormal3dv = caps.get(183);
        this.glNormalPointer = caps.get(184);
        this.glOrtho = caps.get(185);
        this.glPassThrough = caps.get(186);
        this.glPixelMapfv = caps.get(187);
        this.glPixelMapusv = caps.get(188);
        this.glPixelMapuiv = caps.get(189);
        this.glPixelStorei = caps.get(190);
        this.glPixelStoref = caps.get(191);
        this.glPixelTransferi = caps.get(192);
        this.glPixelTransferf = caps.get(193);
        this.glPixelZoom = caps.get(194);
        this.glPointSize = caps.get(195);
        this.glPolygonMode = caps.get(196);
        this.glPolygonOffset = caps.get(197);
        this.glPolygonStipple = caps.get(198);
        this.glPushAttrib = caps.get(199);
        this.glPushClientAttrib = caps.get(200);
        this.glPopAttrib = caps.get(201);
        this.glPopClientAttrib = caps.get(202);
        this.glPopMatrix = caps.get(203);
        this.glPopName = caps.get(204);
        this.glPrioritizeTextures = caps.get(205);
        this.glPushMatrix = caps.get(206);
        this.glPushName = caps.get(207);
        this.glRasterPos2i = caps.get(208);
        this.glRasterPos2s = caps.get(209);
        this.glRasterPos2f = caps.get(210);
        this.glRasterPos2d = caps.get(211);
        this.glRasterPos2iv = caps.get(212);
        this.glRasterPos2sv = caps.get(213);
        this.glRasterPos2fv = caps.get(214);
        this.glRasterPos2dv = caps.get(215);
        this.glRasterPos3i = caps.get(216);
        this.glRasterPos3s = caps.get(217);
        this.glRasterPos3f = caps.get(218);
        this.glRasterPos3d = caps.get(219);
        this.glRasterPos3iv = caps.get(220);
        this.glRasterPos3sv = caps.get(221);
        this.glRasterPos3fv = caps.get(222);
        this.glRasterPos3dv = caps.get(223);
        this.glRasterPos4i = caps.get(224);
        this.glRasterPos4s = caps.get(225);
        this.glRasterPos4f = caps.get(226);
        this.glRasterPos4d = caps.get(227);
        this.glRasterPos4iv = caps.get(228);
        this.glRasterPos4sv = caps.get(229);
        this.glRasterPos4fv = caps.get(230);
        this.glRasterPos4dv = caps.get(231);
        this.glReadBuffer = caps.get(232);
        this.glReadPixels = caps.get(233);
        this.glRecti = caps.get(234);
        this.glRects = caps.get(235);
        this.glRectf = caps.get(236);
        this.glRectd = caps.get(237);
        this.glRectiv = caps.get(238);
        this.glRectsv = caps.get(239);
        this.glRectfv = caps.get(240);
        this.glRectdv = caps.get(241);
        this.glRenderMode = caps.get(242);
        this.glRotatef = caps.get(243);
        this.glRotated = caps.get(244);
        this.glScalef = caps.get(245);
        this.glScaled = caps.get(246);
        this.glScissor = caps.get(247);
        this.glSelectBuffer = caps.get(248);
        this.glShadeModel = caps.get(249);
        this.glStencilFunc = caps.get(250);
        this.glStencilMask = caps.get(251);
        this.glStencilOp = caps.get(252);
        this.glTexCoord1f = caps.get(253);
        this.glTexCoord1s = caps.get(254);
        this.glTexCoord1i = caps.get(255);
        this.glTexCoord1d = caps.get(256);
        this.glTexCoord1fv = caps.get(257);
        this.glTexCoord1sv = caps.get(258);
        this.glTexCoord1iv = caps.get(259);
        this.glTexCoord1dv = caps.get(260);
        this.glTexCoord2f = caps.get(261);
        this.glTexCoord2s = caps.get(262);
        this.glTexCoord2i = caps.get(263);
        this.glTexCoord2d = caps.get(264);
        this.glTexCoord2fv = caps.get(265);
        this.glTexCoord2sv = caps.get(266);
        this.glTexCoord2iv = caps.get(267);
        this.glTexCoord2dv = caps.get(268);
        this.glTexCoord3f = caps.get(269);
        this.glTexCoord3s = caps.get(270);
        this.glTexCoord3i = caps.get(271);
        this.glTexCoord3d = caps.get(272);
        this.glTexCoord3fv = caps.get(273);
        this.glTexCoord3sv = caps.get(274);
        this.glTexCoord3iv = caps.get(275);
        this.glTexCoord3dv = caps.get(276);
        this.glTexCoord4f = caps.get(277);
        this.glTexCoord4s = caps.get(278);
        this.glTexCoord4i = caps.get(279);
        this.glTexCoord4d = caps.get(280);
        this.glTexCoord4fv = caps.get(281);
        this.glTexCoord4sv = caps.get(282);
        this.glTexCoord4iv = caps.get(283);
        this.glTexCoord4dv = caps.get(284);
        this.glTexCoordPointer = caps.get(285);
        this.glTexEnvi = caps.get(286);
        this.glTexEnviv = caps.get(287);
        this.glTexEnvf = caps.get(288);
        this.glTexEnvfv = caps.get(289);
        this.glTexGeni = caps.get(290);
        this.glTexGeniv = caps.get(291);
        this.glTexGenf = caps.get(292);
        this.glTexGenfv = caps.get(293);
        this.glTexGend = caps.get(294);
        this.glTexGendv = caps.get(295);
        this.glTexImage1D = caps.get(296);
        this.glTexImage2D = caps.get(297);
        this.glCopyTexImage1D = caps.get(298);
        this.glCopyTexImage2D = caps.get(299);
        this.glCopyTexSubImage1D = caps.get(300);
        this.glCopyTexSubImage2D = caps.get(301);
        this.glTexParameteri = caps.get(302);
        this.glTexParameteriv = caps.get(303);
        this.glTexParameterf = caps.get(304);
        this.glTexParameterfv = caps.get(305);
        this.glTexSubImage1D = caps.get(306);
        this.glTexSubImage2D = caps.get(307);
        this.glTranslatef = caps.get(308);
        this.glTranslated = caps.get(309);
        this.glVertex2f = caps.get(310);
        this.glVertex2s = caps.get(311);
        this.glVertex2i = caps.get(312);
        this.glVertex2d = caps.get(313);
        this.glVertex2fv = caps.get(314);
        this.glVertex2sv = caps.get(315);
        this.glVertex2iv = caps.get(316);
        this.glVertex2dv = caps.get(317);
        this.glVertex3f = caps.get(318);
        this.glVertex3s = caps.get(319);
        this.glVertex3i = caps.get(320);
        this.glVertex3d = caps.get(321);
        this.glVertex3fv = caps.get(322);
        this.glVertex3sv = caps.get(323);
        this.glVertex3iv = caps.get(324);
        this.glVertex3dv = caps.get(325);
        this.glVertex4f = caps.get(326);
        this.glVertex4s = caps.get(327);
        this.glVertex4i = caps.get(328);
        this.glVertex4d = caps.get(329);
        this.glVertex4fv = caps.get(330);
        this.glVertex4sv = caps.get(331);
        this.glVertex4iv = caps.get(332);
        this.glVertex4dv = caps.get(333);
        this.glVertexPointer = caps.get(334);
        this.glViewport = caps.get(335);
        this.glTexImage3D = caps.get(336);
        this.glTexSubImage3D = caps.get(337);
        this.glCopyTexSubImage3D = caps.get(338);
        this.glDrawRangeElements = caps.get(339);
        this.glCompressedTexImage3D = caps.get(340);
        this.glCompressedTexImage2D = caps.get(341);
        this.glCompressedTexImage1D = caps.get(342);
        this.glCompressedTexSubImage3D = caps.get(343);
        this.glCompressedTexSubImage2D = caps.get(344);
        this.glCompressedTexSubImage1D = caps.get(345);
        this.glGetCompressedTexImage = caps.get(346);
        this.glSampleCoverage = caps.get(347);
        this.glActiveTexture = caps.get(348);
        this.glClientActiveTexture = caps.get(349);
        this.glMultiTexCoord1f = caps.get(350);
        this.glMultiTexCoord1s = caps.get(351);
        this.glMultiTexCoord1i = caps.get(352);
        this.glMultiTexCoord1d = caps.get(353);
        this.glMultiTexCoord1fv = caps.get(354);
        this.glMultiTexCoord1sv = caps.get(355);
        this.glMultiTexCoord1iv = caps.get(356);
        this.glMultiTexCoord1dv = caps.get(357);
        this.glMultiTexCoord2f = caps.get(358);
        this.glMultiTexCoord2s = caps.get(359);
        this.glMultiTexCoord2i = caps.get(360);
        this.glMultiTexCoord2d = caps.get(361);
        this.glMultiTexCoord2fv = caps.get(362);
        this.glMultiTexCoord2sv = caps.get(363);
        this.glMultiTexCoord2iv = caps.get(364);
        this.glMultiTexCoord2dv = caps.get(365);
        this.glMultiTexCoord3f = caps.get(366);
        this.glMultiTexCoord3s = caps.get(367);
        this.glMultiTexCoord3i = caps.get(368);
        this.glMultiTexCoord3d = caps.get(369);
        this.glMultiTexCoord3fv = caps.get(370);
        this.glMultiTexCoord3sv = caps.get(371);
        this.glMultiTexCoord3iv = caps.get(372);
        this.glMultiTexCoord3dv = caps.get(373);
        this.glMultiTexCoord4f = caps.get(374);
        this.glMultiTexCoord4s = caps.get(375);
        this.glMultiTexCoord4i = caps.get(376);
        this.glMultiTexCoord4d = caps.get(377);
        this.glMultiTexCoord4fv = caps.get(378);
        this.glMultiTexCoord4sv = caps.get(379);
        this.glMultiTexCoord4iv = caps.get(380);
        this.glMultiTexCoord4dv = caps.get(381);
        this.glLoadTransposeMatrixf = caps.get(382);
        this.glLoadTransposeMatrixd = caps.get(383);
        this.glMultTransposeMatrixf = caps.get(384);
        this.glMultTransposeMatrixd = caps.get(385);
        this.glBlendColor = caps.get(386);
        this.glBlendEquation = caps.get(387);
        this.glFogCoordf = caps.get(388);
        this.glFogCoordd = caps.get(389);
        this.glFogCoordfv = caps.get(390);
        this.glFogCoorddv = caps.get(391);
        this.glFogCoordPointer = caps.get(392);
        this.glMultiDrawArrays = caps.get(393);
        this.glMultiDrawElements = caps.get(394);
        this.glPointParameterf = caps.get(395);
        this.glPointParameteri = caps.get(396);
        this.glPointParameterfv = caps.get(397);
        this.glPointParameteriv = caps.get(398);
        this.glSecondaryColor3b = caps.get(399);
        this.glSecondaryColor3s = caps.get(400);
        this.glSecondaryColor3i = caps.get(401);
        this.glSecondaryColor3f = caps.get(402);
        this.glSecondaryColor3d = caps.get(403);
        this.glSecondaryColor3ub = caps.get(404);
        this.glSecondaryColor3us = caps.get(405);
        this.glSecondaryColor3ui = caps.get(406);
        this.glSecondaryColor3bv = caps.get(407);
        this.glSecondaryColor3sv = caps.get(408);
        this.glSecondaryColor3iv = caps.get(409);
        this.glSecondaryColor3fv = caps.get(410);
        this.glSecondaryColor3dv = caps.get(411);
        this.glSecondaryColor3ubv = caps.get(412);
        this.glSecondaryColor3usv = caps.get(413);
        this.glSecondaryColor3uiv = caps.get(414);
        this.glSecondaryColorPointer = caps.get(415);
        this.glBlendFuncSeparate = caps.get(416);
        this.glWindowPos2i = caps.get(417);
        this.glWindowPos2s = caps.get(418);
        this.glWindowPos2f = caps.get(419);
        this.glWindowPos2d = caps.get(420);
        this.glWindowPos2iv = caps.get(421);
        this.glWindowPos2sv = caps.get(422);
        this.glWindowPos2fv = caps.get(423);
        this.glWindowPos2dv = caps.get(424);
        this.glWindowPos3i = caps.get(425);
        this.glWindowPos3s = caps.get(426);
        this.glWindowPos3f = caps.get(427);
        this.glWindowPos3d = caps.get(428);
        this.glWindowPos3iv = caps.get(429);
        this.glWindowPos3sv = caps.get(430);
        this.glWindowPos3fv = caps.get(431);
        this.glWindowPos3dv = caps.get(432);
        this.glBindBuffer = caps.get(433);
        this.glDeleteBuffers = caps.get(434);
        this.glGenBuffers = caps.get(435);
        this.glIsBuffer = caps.get(436);
        this.glBufferData = caps.get(437);
        this.glBufferSubData = caps.get(438);
        this.glGetBufferSubData = caps.get(439);
        this.glMapBuffer = caps.get(440);
        this.glUnmapBuffer = caps.get(441);
        this.glGetBufferParameteriv = caps.get(442);
        this.glGetBufferPointerv = caps.get(443);
        this.glGenQueries = caps.get(444);
        this.glDeleteQueries = caps.get(445);
        this.glIsQuery = caps.get(446);
        this.glBeginQuery = caps.get(447);
        this.glEndQuery = caps.get(448);
        this.glGetQueryiv = caps.get(449);
        this.glGetQueryObjectiv = caps.get(450);
        this.glGetQueryObjectuiv = caps.get(451);
        this.glCreateProgram = caps.get(452);
        this.glDeleteProgram = caps.get(453);
        this.glIsProgram = caps.get(454);
        this.glCreateShader = caps.get(455);
        this.glDeleteShader = caps.get(456);
        this.glIsShader = caps.get(457);
        this.glAttachShader = caps.get(458);
        this.glDetachShader = caps.get(459);
        this.glShaderSource = caps.get(460);
        this.glCompileShader = caps.get(461);
        this.glLinkProgram = caps.get(462);
        this.glUseProgram = caps.get(463);
        this.glValidateProgram = caps.get(464);
        this.glUniform1f = caps.get(465);
        this.glUniform2f = caps.get(466);
        this.glUniform3f = caps.get(467);
        this.glUniform4f = caps.get(468);
        this.glUniform1i = caps.get(469);
        this.glUniform2i = caps.get(470);
        this.glUniform3i = caps.get(471);
        this.glUniform4i = caps.get(472);
        this.glUniform1fv = caps.get(473);
        this.glUniform2fv = caps.get(474);
        this.glUniform3fv = caps.get(475);
        this.glUniform4fv = caps.get(476);
        this.glUniform1iv = caps.get(477);
        this.glUniform2iv = caps.get(478);
        this.glUniform3iv = caps.get(479);
        this.glUniform4iv = caps.get(480);
        this.glUniformMatrix2fv = caps.get(481);
        this.glUniformMatrix3fv = caps.get(482);
        this.glUniformMatrix4fv = caps.get(483);
        this.glGetShaderiv = caps.get(484);
        this.glGetProgramiv = caps.get(485);
        this.glGetShaderInfoLog = caps.get(486);
        this.glGetProgramInfoLog = caps.get(487);
        this.glGetAttachedShaders = caps.get(488);
        this.glGetUniformLocation = caps.get(489);
        this.glGetActiveUniform = caps.get(490);
        this.glGetUniformfv = caps.get(491);
        this.glGetUniformiv = caps.get(492);
        this.glGetShaderSource = caps.get(493);
        this.glVertexAttrib1f = caps.get(494);
        this.glVertexAttrib1s = caps.get(495);
        this.glVertexAttrib1d = caps.get(496);
        this.glVertexAttrib2f = caps.get(497);
        this.glVertexAttrib2s = caps.get(498);
        this.glVertexAttrib2d = caps.get(499);
        this.glVertexAttrib3f = caps.get(500);
        this.glVertexAttrib3s = caps.get(501);
        this.glVertexAttrib3d = caps.get(502);
        this.glVertexAttrib4f = caps.get(503);
        this.glVertexAttrib4s = caps.get(504);
        this.glVertexAttrib4d = caps.get(505);
        this.glVertexAttrib4Nub = caps.get(506);
        this.glVertexAttrib1fv = caps.get(507);
        this.glVertexAttrib1sv = caps.get(508);
        this.glVertexAttrib1dv = caps.get(509);
        this.glVertexAttrib2fv = caps.get(510);
        this.glVertexAttrib2sv = caps.get(511);
        this.glVertexAttrib2dv = caps.get(512);
        this.glVertexAttrib3fv = caps.get(513);
        this.glVertexAttrib3sv = caps.get(514);
        this.glVertexAttrib3dv = caps.get(515);
        this.glVertexAttrib4fv = caps.get(516);
        this.glVertexAttrib4sv = caps.get(517);
        this.glVertexAttrib4dv = caps.get(518);
        this.glVertexAttrib4iv = caps.get(519);
        this.glVertexAttrib4bv = caps.get(520);
        this.glVertexAttrib4ubv = caps.get(521);
        this.glVertexAttrib4usv = caps.get(522);
        this.glVertexAttrib4uiv = caps.get(523);
        this.glVertexAttrib4Nbv = caps.get(524);
        this.glVertexAttrib4Nsv = caps.get(525);
        this.glVertexAttrib4Niv = caps.get(526);
        this.glVertexAttrib4Nubv = caps.get(527);
        this.glVertexAttrib4Nusv = caps.get(528);
        this.glVertexAttrib4Nuiv = caps.get(529);
        this.glVertexAttribPointer = caps.get(530);
        this.glEnableVertexAttribArray = caps.get(531);
        this.glDisableVertexAttribArray = caps.get(532);
        this.glBindAttribLocation = caps.get(533);
        this.glGetActiveAttrib = caps.get(534);
        this.glGetAttribLocation = caps.get(535);
        this.glGetVertexAttribiv = caps.get(536);
        this.glGetVertexAttribfv = caps.get(537);
        this.glGetVertexAttribdv = caps.get(538);
        this.glGetVertexAttribPointerv = caps.get(539);
        this.glDrawBuffers = caps.get(540);
        this.glBlendEquationSeparate = caps.get(541);
        this.glStencilOpSeparate = caps.get(542);
        this.glStencilFuncSeparate = caps.get(543);
        this.glStencilMaskSeparate = caps.get(544);
        this.glUniformMatrix2x3fv = caps.get(545);
        this.glUniformMatrix3x2fv = caps.get(546);
        this.glUniformMatrix2x4fv = caps.get(547);
        this.glUniformMatrix4x2fv = caps.get(548);
        this.glUniformMatrix3x4fv = caps.get(549);
        this.glUniformMatrix4x3fv = caps.get(550);
        this.glGetStringi = caps.get(551);
        this.glClearBufferiv = caps.get(552);
        this.glClearBufferuiv = caps.get(553);
        this.glClearBufferfv = caps.get(554);
        this.glClearBufferfi = caps.get(555);
        this.glVertexAttribI1i = caps.get(556);
        this.glVertexAttribI2i = caps.get(557);
        this.glVertexAttribI3i = caps.get(558);
        this.glVertexAttribI4i = caps.get(559);
        this.glVertexAttribI1ui = caps.get(560);
        this.glVertexAttribI2ui = caps.get(561);
        this.glVertexAttribI3ui = caps.get(562);
        this.glVertexAttribI4ui = caps.get(563);
        this.glVertexAttribI1iv = caps.get(564);
        this.glVertexAttribI2iv = caps.get(565);
        this.glVertexAttribI3iv = caps.get(566);
        this.glVertexAttribI4iv = caps.get(567);
        this.glVertexAttribI1uiv = caps.get(568);
        this.glVertexAttribI2uiv = caps.get(569);
        this.glVertexAttribI3uiv = caps.get(570);
        this.glVertexAttribI4uiv = caps.get(571);
        this.glVertexAttribI4bv = caps.get(572);
        this.glVertexAttribI4sv = caps.get(573);
        this.glVertexAttribI4ubv = caps.get(574);
        this.glVertexAttribI4usv = caps.get(575);
        this.glVertexAttribIPointer = caps.get(576);
        this.glGetVertexAttribIiv = caps.get(577);
        this.glGetVertexAttribIuiv = caps.get(578);
        this.glUniform1ui = caps.get(579);
        this.glUniform2ui = caps.get(580);
        this.glUniform3ui = caps.get(581);
        this.glUniform4ui = caps.get(582);
        this.glUniform1uiv = caps.get(583);
        this.glUniform2uiv = caps.get(584);
        this.glUniform3uiv = caps.get(585);
        this.glUniform4uiv = caps.get(586);
        this.glGetUniformuiv = caps.get(587);
        this.glBindFragDataLocation = caps.get(588);
        this.glGetFragDataLocation = caps.get(589);
        this.glBeginConditionalRender = caps.get(590);
        this.glEndConditionalRender = caps.get(591);
        this.glMapBufferRange = caps.get(592);
        this.glFlushMappedBufferRange = caps.get(593);
        this.glClampColor = caps.get(594);
        this.glIsRenderbuffer = caps.get(595);
        this.glBindRenderbuffer = caps.get(596);
        this.glDeleteRenderbuffers = caps.get(597);
        this.glGenRenderbuffers = caps.get(598);
        this.glRenderbufferStorage = caps.get(599);
        this.glRenderbufferStorageMultisample = caps.get(600);
        this.glGetRenderbufferParameteriv = caps.get(601);
        this.glIsFramebuffer = caps.get(602);
        this.glBindFramebuffer = caps.get(603);
        this.glDeleteFramebuffers = caps.get(604);
        this.glGenFramebuffers = caps.get(605);
        this.glCheckFramebufferStatus = caps.get(606);
        this.glFramebufferTexture1D = caps.get(607);
        this.glFramebufferTexture2D = caps.get(608);
        this.glFramebufferTexture3D = caps.get(609);
        this.glFramebufferTextureLayer = caps.get(610);
        this.glFramebufferRenderbuffer = caps.get(611);
        this.glGetFramebufferAttachmentParameteriv = caps.get(612);
        this.glBlitFramebuffer = caps.get(613);
        this.glGenerateMipmap = caps.get(614);
        this.glTexParameterIiv = caps.get(615);
        this.glTexParameterIuiv = caps.get(616);
        this.glGetTexParameterIiv = caps.get(617);
        this.glGetTexParameterIuiv = caps.get(618);
        this.glColorMaski = caps.get(619);
        this.glGetBooleani_v = caps.get(620);
        this.glGetIntegeri_v = caps.get(621);
        this.glEnablei = caps.get(622);
        this.glDisablei = caps.get(623);
        this.glIsEnabledi = caps.get(624);
        this.glBindBufferRange = caps.get(625);
        this.glBindBufferBase = caps.get(626);
        this.glBeginTransformFeedback = caps.get(627);
        this.glEndTransformFeedback = caps.get(628);
        this.glTransformFeedbackVaryings = caps.get(629);
        this.glGetTransformFeedbackVarying = caps.get(630);
        this.glBindVertexArray = caps.get(631);
        this.glDeleteVertexArrays = caps.get(632);
        this.glGenVertexArrays = caps.get(633);
        this.glIsVertexArray = caps.get(634);
        this.glDrawArraysInstanced = caps.get(635);
        this.glDrawElementsInstanced = caps.get(636);
        this.glCopyBufferSubData = caps.get(637);
        this.glPrimitiveRestartIndex = caps.get(638);
        this.glTexBuffer = caps.get(639);
        this.glGetUniformIndices = caps.get(640);
        this.glGetActiveUniformsiv = caps.get(641);
        this.glGetActiveUniformName = caps.get(642);
        this.glGetUniformBlockIndex = caps.get(643);
        this.glGetActiveUniformBlockiv = caps.get(644);
        this.glGetActiveUniformBlockName = caps.get(645);
        this.glUniformBlockBinding = caps.get(646);
        this.glGetBufferParameteri64v = caps.get(647);
        this.glDrawElementsBaseVertex = caps.get(648);
        this.glDrawRangeElementsBaseVertex = caps.get(649);
        this.glDrawElementsInstancedBaseVertex = caps.get(650);
        this.glMultiDrawElementsBaseVertex = caps.get(651);
        this.glProvokingVertex = caps.get(652);
        this.glTexImage2DMultisample = caps.get(653);
        this.glTexImage3DMultisample = caps.get(654);
        this.glGetMultisamplefv = caps.get(655);
        this.glSampleMaski = caps.get(656);
        this.glFramebufferTexture = caps.get(657);
        this.glFenceSync = caps.get(658);
        this.glIsSync = caps.get(659);
        this.glDeleteSync = caps.get(660);
        this.glClientWaitSync = caps.get(661);
        this.glWaitSync = caps.get(662);
        this.glGetInteger64v = caps.get(663);
        this.glGetInteger64i_v = caps.get(664);
        this.glGetSynciv = caps.get(665);
        this.glBindFragDataLocationIndexed = caps.get(666);
        this.glGetFragDataIndex = caps.get(667);
        this.glGenSamplers = caps.get(668);
        this.glDeleteSamplers = caps.get(669);
        this.glIsSampler = caps.get(670);
        this.glBindSampler = caps.get(671);
        this.glSamplerParameteri = caps.get(672);
        this.glSamplerParameterf = caps.get(673);
        this.glSamplerParameteriv = caps.get(674);
        this.glSamplerParameterfv = caps.get(675);
        this.glSamplerParameterIiv = caps.get(676);
        this.glSamplerParameterIuiv = caps.get(677);
        this.glGetSamplerParameteriv = caps.get(678);
        this.glGetSamplerParameterfv = caps.get(679);
        this.glGetSamplerParameterIiv = caps.get(680);
        this.glGetSamplerParameterIuiv = caps.get(681);
        this.glQueryCounter = caps.get(682);
        this.glGetQueryObjecti64v = caps.get(683);
        this.glGetQueryObjectui64v = caps.get(684);
        this.glVertexAttribDivisor = caps.get(685);
        this.glVertexP2ui = caps.get(686);
        this.glVertexP3ui = caps.get(687);
        this.glVertexP4ui = caps.get(688);
        this.glVertexP2uiv = caps.get(689);
        this.glVertexP3uiv = caps.get(690);
        this.glVertexP4uiv = caps.get(691);
        this.glTexCoordP1ui = caps.get(692);
        this.glTexCoordP2ui = caps.get(693);
        this.glTexCoordP3ui = caps.get(694);
        this.glTexCoordP4ui = caps.get(695);
        this.glTexCoordP1uiv = caps.get(696);
        this.glTexCoordP2uiv = caps.get(697);
        this.glTexCoordP3uiv = caps.get(698);
        this.glTexCoordP4uiv = caps.get(699);
        this.glMultiTexCoordP1ui = caps.get(700);
        this.glMultiTexCoordP2ui = caps.get(701);
        this.glMultiTexCoordP3ui = caps.get(702);
        this.glMultiTexCoordP4ui = caps.get(703);
        this.glMultiTexCoordP1uiv = caps.get(704);
        this.glMultiTexCoordP2uiv = caps.get(705);
        this.glMultiTexCoordP3uiv = caps.get(706);
        this.glMultiTexCoordP4uiv = caps.get(707);
        this.glNormalP3ui = caps.get(708);
        this.glNormalP3uiv = caps.get(709);
        this.glColorP3ui = caps.get(710);
        this.glColorP4ui = caps.get(711);
        this.glColorP3uiv = caps.get(712);
        this.glColorP4uiv = caps.get(713);
        this.glSecondaryColorP3ui = caps.get(714);
        this.glSecondaryColorP3uiv = caps.get(715);
        this.glVertexAttribP1ui = caps.get(716);
        this.glVertexAttribP2ui = caps.get(717);
        this.glVertexAttribP3ui = caps.get(718);
        this.glVertexAttribP4ui = caps.get(719);
        this.glVertexAttribP1uiv = caps.get(720);
        this.glVertexAttribP2uiv = caps.get(721);
        this.glVertexAttribP3uiv = caps.get(722);
        this.glVertexAttribP4uiv = caps.get(723);
        this.glBlendEquationi = caps.get(724);
        this.glBlendEquationSeparatei = caps.get(725);
        this.glBlendFunci = caps.get(726);
        this.glBlendFuncSeparatei = caps.get(727);
        this.glDrawArraysIndirect = caps.get(728);
        this.glDrawElementsIndirect = caps.get(729);
        this.glUniform1d = caps.get(730);
        this.glUniform2d = caps.get(731);
        this.glUniform3d = caps.get(732);
        this.glUniform4d = caps.get(733);
        this.glUniform1dv = caps.get(734);
        this.glUniform2dv = caps.get(735);
        this.glUniform3dv = caps.get(736);
        this.glUniform4dv = caps.get(737);
        this.glUniformMatrix2dv = caps.get(738);
        this.glUniformMatrix3dv = caps.get(739);
        this.glUniformMatrix4dv = caps.get(740);
        this.glUniformMatrix2x3dv = caps.get(741);
        this.glUniformMatrix2x4dv = caps.get(742);
        this.glUniformMatrix3x2dv = caps.get(743);
        this.glUniformMatrix3x4dv = caps.get(744);
        this.glUniformMatrix4x2dv = caps.get(745);
        this.glUniformMatrix4x3dv = caps.get(746);
        this.glGetUniformdv = caps.get(747);
        this.glMinSampleShading = caps.get(748);
        this.glGetSubroutineUniformLocation = caps.get(749);
        this.glGetSubroutineIndex = caps.get(750);
        this.glGetActiveSubroutineUniformiv = caps.get(751);
        this.glGetActiveSubroutineUniformName = caps.get(752);
        this.glGetActiveSubroutineName = caps.get(753);
        this.glUniformSubroutinesuiv = caps.get(754);
        this.glGetUniformSubroutineuiv = caps.get(755);
        this.glGetProgramStageiv = caps.get(756);
        this.glPatchParameteri = caps.get(757);
        this.glPatchParameterfv = caps.get(758);
        this.glBindTransformFeedback = caps.get(759);
        this.glDeleteTransformFeedbacks = caps.get(760);
        this.glGenTransformFeedbacks = caps.get(761);
        this.glIsTransformFeedback = caps.get(762);
        this.glPauseTransformFeedback = caps.get(763);
        this.glResumeTransformFeedback = caps.get(764);
        this.glDrawTransformFeedback = caps.get(765);
        this.glDrawTransformFeedbackStream = caps.get(766);
        this.glBeginQueryIndexed = caps.get(767);
        this.glEndQueryIndexed = caps.get(768);
        this.glGetQueryIndexediv = caps.get(769);
        this.glReleaseShaderCompiler = caps.get(770);
        this.glShaderBinary = caps.get(771);
        this.glGetShaderPrecisionFormat = caps.get(772);
        this.glDepthRangef = caps.get(773);
        this.glClearDepthf = caps.get(774);
        this.glGetProgramBinary = caps.get(775);
        this.glProgramBinary = caps.get(776);
        this.glProgramParameteri = caps.get(777);
        this.glUseProgramStages = caps.get(778);
        this.glActiveShaderProgram = caps.get(779);
        this.glCreateShaderProgramv = caps.get(780);
        this.glBindProgramPipeline = caps.get(781);
        this.glDeleteProgramPipelines = caps.get(782);
        this.glGenProgramPipelines = caps.get(783);
        this.glIsProgramPipeline = caps.get(784);
        this.glGetProgramPipelineiv = caps.get(785);
        this.glProgramUniform1i = caps.get(786);
        this.glProgramUniform2i = caps.get(787);
        this.glProgramUniform3i = caps.get(788);
        this.glProgramUniform4i = caps.get(789);
        this.glProgramUniform1ui = caps.get(790);
        this.glProgramUniform2ui = caps.get(791);
        this.glProgramUniform3ui = caps.get(792);
        this.glProgramUniform4ui = caps.get(793);
        this.glProgramUniform1f = caps.get(794);
        this.glProgramUniform2f = caps.get(795);
        this.glProgramUniform3f = caps.get(796);
        this.glProgramUniform4f = caps.get(797);
        this.glProgramUniform1d = caps.get(798);
        this.glProgramUniform2d = caps.get(799);
        this.glProgramUniform3d = caps.get(800);
        this.glProgramUniform4d = caps.get(801);
        this.glProgramUniform1iv = caps.get(802);
        this.glProgramUniform2iv = caps.get(803);
        this.glProgramUniform3iv = caps.get(804);
        this.glProgramUniform4iv = caps.get(805);
        this.glProgramUniform1uiv = caps.get(806);
        this.glProgramUniform2uiv = caps.get(807);
        this.glProgramUniform3uiv = caps.get(808);
        this.glProgramUniform4uiv = caps.get(809);
        this.glProgramUniform1fv = caps.get(810);
        this.glProgramUniform2fv = caps.get(811);
        this.glProgramUniform3fv = caps.get(812);
        this.glProgramUniform4fv = caps.get(813);
        this.glProgramUniform1dv = caps.get(814);
        this.glProgramUniform2dv = caps.get(815);
        this.glProgramUniform3dv = caps.get(816);
        this.glProgramUniform4dv = caps.get(817);
        this.glProgramUniformMatrix2fv = caps.get(818);
        this.glProgramUniformMatrix3fv = caps.get(819);
        this.glProgramUniformMatrix4fv = caps.get(820);
        this.glProgramUniformMatrix2dv = caps.get(821);
        this.glProgramUniformMatrix3dv = caps.get(822);
        this.glProgramUniformMatrix4dv = caps.get(823);
        this.glProgramUniformMatrix2x3fv = caps.get(824);
        this.glProgramUniformMatrix3x2fv = caps.get(825);
        this.glProgramUniformMatrix2x4fv = caps.get(826);
        this.glProgramUniformMatrix4x2fv = caps.get(827);
        this.glProgramUniformMatrix3x4fv = caps.get(828);
        this.glProgramUniformMatrix4x3fv = caps.get(829);
        this.glProgramUniformMatrix2x3dv = caps.get(830);
        this.glProgramUniformMatrix3x2dv = caps.get(831);
        this.glProgramUniformMatrix2x4dv = caps.get(832);
        this.glProgramUniformMatrix4x2dv = caps.get(833);
        this.glProgramUniformMatrix3x4dv = caps.get(834);
        this.glProgramUniformMatrix4x3dv = caps.get(835);
        this.glValidateProgramPipeline = caps.get(836);
        this.glGetProgramPipelineInfoLog = caps.get(837);
        this.glVertexAttribL1d = caps.get(838);
        this.glVertexAttribL2d = caps.get(839);
        this.glVertexAttribL3d = caps.get(840);
        this.glVertexAttribL4d = caps.get(841);
        this.glVertexAttribL1dv = caps.get(842);
        this.glVertexAttribL2dv = caps.get(843);
        this.glVertexAttribL3dv = caps.get(844);
        this.glVertexAttribL4dv = caps.get(845);
        this.glVertexAttribLPointer = caps.get(846);
        this.glGetVertexAttribLdv = caps.get(847);
        this.glViewportArrayv = caps.get(848);
        this.glViewportIndexedf = caps.get(849);
        this.glViewportIndexedfv = caps.get(850);
        this.glScissorArrayv = caps.get(851);
        this.glScissorIndexed = caps.get(852);
        this.glScissorIndexedv = caps.get(853);
        this.glDepthRangeArrayv = caps.get(854);
        this.glDepthRangeIndexed = caps.get(855);
        this.glGetFloati_v = caps.get(856);
        this.glGetDoublei_v = caps.get(857);
        this.glGetActiveAtomicCounterBufferiv = caps.get(858);
        this.glTexStorage1D = caps.get(859);
        this.glTexStorage2D = caps.get(860);
        this.glTexStorage3D = caps.get(861);
        this.glDrawTransformFeedbackInstanced = caps.get(862);
        this.glDrawTransformFeedbackStreamInstanced = caps.get(863);
        this.glDrawArraysInstancedBaseInstance = caps.get(864);
        this.glDrawElementsInstancedBaseInstance = caps.get(865);
        this.glDrawElementsInstancedBaseVertexBaseInstance = caps.get(866);
        this.glBindImageTexture = caps.get(867);
        this.glMemoryBarrier = caps.get(868);
        this.glGetInternalformativ = caps.get(869);
        this.glClearBufferData = caps.get(870);
        this.glClearBufferSubData = caps.get(871);
        this.glDispatchCompute = caps.get(872);
        this.glDispatchComputeIndirect = caps.get(873);
        this.glCopyImageSubData = caps.get(874);
        this.glDebugMessageControl = caps.get(875);
        this.glDebugMessageInsert = caps.get(876);
        this.glDebugMessageCallback = caps.get(877);
        this.glGetDebugMessageLog = caps.get(878);
        this.glPushDebugGroup = caps.get(879);
        this.glPopDebugGroup = caps.get(880);
        this.glObjectLabel = caps.get(881);
        this.glGetObjectLabel = caps.get(882);
        this.glObjectPtrLabel = caps.get(883);
        this.glGetObjectPtrLabel = caps.get(884);
        this.glFramebufferParameteri = caps.get(885);
        this.glGetFramebufferParameteriv = caps.get(886);
        this.glGetInternalformati64v = caps.get(887);
        this.glInvalidateTexSubImage = caps.get(888);
        this.glInvalidateTexImage = caps.get(889);
        this.glInvalidateBufferSubData = caps.get(890);
        this.glInvalidateBufferData = caps.get(891);
        this.glInvalidateFramebuffer = caps.get(892);
        this.glInvalidateSubFramebuffer = caps.get(893);
        this.glMultiDrawArraysIndirect = caps.get(894);
        this.glMultiDrawElementsIndirect = caps.get(895);
        this.glGetProgramInterfaceiv = caps.get(896);
        this.glGetProgramResourceIndex = caps.get(897);
        this.glGetProgramResourceName = caps.get(898);
        this.glGetProgramResourceiv = caps.get(899);
        this.glGetProgramResourceLocation = caps.get(900);
        this.glGetProgramResourceLocationIndex = caps.get(901);
        this.glShaderStorageBlockBinding = caps.get(902);
        this.glTexBufferRange = caps.get(903);
        this.glTexStorage2DMultisample = caps.get(904);
        this.glTexStorage3DMultisample = caps.get(905);
        this.glTextureView = caps.get(906);
        this.glBindVertexBuffer = caps.get(907);
        this.glVertexAttribFormat = caps.get(908);
        this.glVertexAttribIFormat = caps.get(909);
        this.glVertexAttribLFormat = caps.get(910);
        this.glVertexAttribBinding = caps.get(911);
        this.glVertexBindingDivisor = caps.get(912);
        this.glBufferStorage = caps.get(913);
        this.glClearTexSubImage = caps.get(914);
        this.glClearTexImage = caps.get(915);
        this.glBindBuffersBase = caps.get(916);
        this.glBindBuffersRange = caps.get(917);
        this.glBindTextures = caps.get(918);
        this.glBindSamplers = caps.get(919);
        this.glBindImageTextures = caps.get(920);
        this.glBindVertexBuffers = caps.get(921);
        this.glClipControl = caps.get(922);
        this.glCreateTransformFeedbacks = caps.get(923);
        this.glTransformFeedbackBufferBase = caps.get(924);
        this.glTransformFeedbackBufferRange = caps.get(925);
        this.glGetTransformFeedbackiv = caps.get(926);
        this.glGetTransformFeedbacki_v = caps.get(927);
        this.glGetTransformFeedbacki64_v = caps.get(928);
        this.glCreateBuffers = caps.get(929);
        this.glNamedBufferStorage = caps.get(930);
        this.glNamedBufferData = caps.get(931);
        this.glNamedBufferSubData = caps.get(932);
        this.glCopyNamedBufferSubData = caps.get(933);
        this.glClearNamedBufferData = caps.get(934);
        this.glClearNamedBufferSubData = caps.get(935);
        this.glMapNamedBuffer = caps.get(936);
        this.glMapNamedBufferRange = caps.get(937);
        this.glUnmapNamedBuffer = caps.get(938);
        this.glFlushMappedNamedBufferRange = caps.get(939);
        this.glGetNamedBufferParameteriv = caps.get(940);
        this.glGetNamedBufferParameteri64v = caps.get(941);
        this.glGetNamedBufferPointerv = caps.get(942);
        this.glGetNamedBufferSubData = caps.get(943);
        this.glCreateFramebuffers = caps.get(944);
        this.glNamedFramebufferRenderbuffer = caps.get(945);
        this.glNamedFramebufferParameteri = caps.get(946);
        this.glNamedFramebufferTexture = caps.get(947);
        this.glNamedFramebufferTextureLayer = caps.get(948);
        this.glNamedFramebufferDrawBuffer = caps.get(949);
        this.glNamedFramebufferDrawBuffers = caps.get(950);
        this.glNamedFramebufferReadBuffer = caps.get(951);
        this.glInvalidateNamedFramebufferData = caps.get(952);
        this.glInvalidateNamedFramebufferSubData = caps.get(953);
        this.glClearNamedFramebufferiv = caps.get(954);
        this.glClearNamedFramebufferuiv = caps.get(955);
        this.glClearNamedFramebufferfv = caps.get(956);
        this.glClearNamedFramebufferfi = caps.get(957);
        this.glBlitNamedFramebuffer = caps.get(958);
        this.glCheckNamedFramebufferStatus = caps.get(959);
        this.glGetNamedFramebufferParameteriv = caps.get(960);
        this.glGetNamedFramebufferAttachmentParameteriv = caps.get(961);
        this.glCreateRenderbuffers = caps.get(962);
        this.glNamedRenderbufferStorage = caps.get(963);
        this.glNamedRenderbufferStorageMultisample = caps.get(964);
        this.glGetNamedRenderbufferParameteriv = caps.get(965);
        this.glCreateTextures = caps.get(966);
        this.glTextureBuffer = caps.get(967);
        this.glTextureBufferRange = caps.get(968);
        this.glTextureStorage1D = caps.get(969);
        this.glTextureStorage2D = caps.get(970);
        this.glTextureStorage3D = caps.get(971);
        this.glTextureStorage2DMultisample = caps.get(972);
        this.glTextureStorage3DMultisample = caps.get(973);
        this.glTextureSubImage1D = caps.get(974);
        this.glTextureSubImage2D = caps.get(975);
        this.glTextureSubImage3D = caps.get(976);
        this.glCompressedTextureSubImage1D = caps.get(977);
        this.glCompressedTextureSubImage2D = caps.get(978);
        this.glCompressedTextureSubImage3D = caps.get(979);
        this.glCopyTextureSubImage1D = caps.get(980);
        this.glCopyTextureSubImage2D = caps.get(981);
        this.glCopyTextureSubImage3D = caps.get(982);
        this.glTextureParameterf = caps.get(983);
        this.glTextureParameterfv = caps.get(984);
        this.glTextureParameteri = caps.get(985);
        this.glTextureParameterIiv = caps.get(986);
        this.glTextureParameterIuiv = caps.get(987);
        this.glTextureParameteriv = caps.get(988);
        this.glGenerateTextureMipmap = caps.get(989);
        this.glBindTextureUnit = caps.get(990);
        this.glGetTextureImage = caps.get(991);
        this.glGetCompressedTextureImage = caps.get(992);
        this.glGetTextureLevelParameterfv = caps.get(993);
        this.glGetTextureLevelParameteriv = caps.get(994);
        this.glGetTextureParameterfv = caps.get(995);
        this.glGetTextureParameterIiv = caps.get(996);
        this.glGetTextureParameterIuiv = caps.get(997);
        this.glGetTextureParameteriv = caps.get(998);
        this.glCreateVertexArrays = caps.get(999);
        this.glDisableVertexArrayAttrib = caps.get(1000);
        this.glEnableVertexArrayAttrib = caps.get(1001);
        this.glVertexArrayElementBuffer = caps.get(1002);
        this.glVertexArrayVertexBuffer = caps.get(1003);
        this.glVertexArrayVertexBuffers = caps.get(1004);
        this.glVertexArrayAttribFormat = caps.get(1005);
        this.glVertexArrayAttribIFormat = caps.get(1006);
        this.glVertexArrayAttribLFormat = caps.get(1007);
        this.glVertexArrayAttribBinding = caps.get(1008);
        this.glVertexArrayBindingDivisor = caps.get(1009);
        this.glGetVertexArrayiv = caps.get(1010);
        this.glGetVertexArrayIndexediv = caps.get(1011);
        this.glGetVertexArrayIndexed64iv = caps.get(1012);
        this.glCreateSamplers = caps.get(1013);
        this.glCreateProgramPipelines = caps.get(1014);
        this.glCreateQueries = caps.get(1015);
        this.glGetQueryBufferObjectiv = caps.get(1016);
        this.glGetQueryBufferObjectuiv = caps.get(1017);
        this.glGetQueryBufferObjecti64v = caps.get(1018);
        this.glGetQueryBufferObjectui64v = caps.get(1019);
        this.glMemoryBarrierByRegion = caps.get(1020);
        this.glGetTextureSubImage = caps.get(1021);
        this.glGetCompressedTextureSubImage = caps.get(1022);
        this.glTextureBarrier = caps.get(1023);
        this.glGetGraphicsResetStatus = caps.get(1024);
        this.glGetnMapdv = caps.get(1025);
        this.glGetnMapfv = caps.get(1026);
        this.glGetnMapiv = caps.get(1027);
        this.glGetnPixelMapfv = caps.get(1028);
        this.glGetnPixelMapuiv = caps.get(1029);
        this.glGetnPixelMapusv = caps.get(1030);
        this.glGetnPolygonStipple = caps.get(1031);
        this.glGetnTexImage = caps.get(1032);
        this.glReadnPixels = caps.get(1033);
        this.glGetnColorTable = caps.get(1034);
        this.glGetnConvolutionFilter = caps.get(1035);
        this.glGetnSeparableFilter = caps.get(1036);
        this.glGetnHistogram = caps.get(1037);
        this.glGetnMinmax = caps.get(1038);
        this.glGetnCompressedTexImage = caps.get(1039);
        this.glGetnUniformfv = caps.get(1040);
        this.glGetnUniformdv = caps.get(1041);
        this.glGetnUniformiv = caps.get(1042);
        this.glGetnUniformuiv = caps.get(1043);
        this.glMultiDrawArraysIndirectCount = caps.get(1044);
        this.glMultiDrawElementsIndirectCount = caps.get(1045);
        this.glPolygonOffsetClamp = caps.get(1046);
        this.glSpecializeShader = caps.get(1047);
        this.glDebugMessageEnableAMD = caps.get(1048);
        this.glDebugMessageInsertAMD = caps.get(1049);
        this.glDebugMessageCallbackAMD = caps.get(1050);
        this.glGetDebugMessageLogAMD = caps.get(1051);
        this.glBlendFuncIndexedAMD = caps.get(1052);
        this.glBlendFuncSeparateIndexedAMD = caps.get(1053);
        this.glBlendEquationIndexedAMD = caps.get(1054);
        this.glBlendEquationSeparateIndexedAMD = caps.get(1055);
        this.glRenderbufferStorageMultisampleAdvancedAMD = caps.get(1056);
        this.glNamedRenderbufferStorageMultisampleAdvancedAMD = caps.get(1057);
        this.glUniform1i64NV = caps.get(1058);
        this.glUniform2i64NV = caps.get(1059);
        this.glUniform3i64NV = caps.get(1060);
        this.glUniform4i64NV = caps.get(1061);
        this.glUniform1i64vNV = caps.get(1062);
        this.glUniform2i64vNV = caps.get(1063);
        this.glUniform3i64vNV = caps.get(1064);
        this.glUniform4i64vNV = caps.get(1065);
        this.glUniform1ui64NV = caps.get(1066);
        this.glUniform2ui64NV = caps.get(1067);
        this.glUniform3ui64NV = caps.get(1068);
        this.glUniform4ui64NV = caps.get(1069);
        this.glUniform1ui64vNV = caps.get(1070);
        this.glUniform2ui64vNV = caps.get(1071);
        this.glUniform3ui64vNV = caps.get(1072);
        this.glUniform4ui64vNV = caps.get(1073);
        this.glGetUniformi64vNV = caps.get(1074);
        this.glGetUniformui64vNV = caps.get(1075);
        this.glProgramUniform1i64NV = caps.get(1076);
        this.glProgramUniform2i64NV = caps.get(1077);
        this.glProgramUniform3i64NV = caps.get(1078);
        this.glProgramUniform4i64NV = caps.get(1079);
        this.glProgramUniform1i64vNV = caps.get(1080);
        this.glProgramUniform2i64vNV = caps.get(1081);
        this.glProgramUniform3i64vNV = caps.get(1082);
        this.glProgramUniform4i64vNV = caps.get(1083);
        this.glProgramUniform1ui64NV = caps.get(1084);
        this.glProgramUniform2ui64NV = caps.get(1085);
        this.glProgramUniform3ui64NV = caps.get(1086);
        this.glProgramUniform4ui64NV = caps.get(1087);
        this.glProgramUniform1ui64vNV = caps.get(1088);
        this.glProgramUniform2ui64vNV = caps.get(1089);
        this.glProgramUniform3ui64vNV = caps.get(1090);
        this.glProgramUniform4ui64vNV = caps.get(1091);
        this.glVertexAttribParameteriAMD = caps.get(1092);
        this.glQueryObjectParameteruiAMD = caps.get(1093);
        this.glGetPerfMonitorGroupsAMD = caps.get(1094);
        this.glGetPerfMonitorCountersAMD = caps.get(1095);
        this.glGetPerfMonitorGroupStringAMD = caps.get(1096);
        this.glGetPerfMonitorCounterStringAMD = caps.get(1097);
        this.glGetPerfMonitorCounterInfoAMD = caps.get(1098);
        this.glGenPerfMonitorsAMD = caps.get(1099);
        this.glDeletePerfMonitorsAMD = caps.get(1100);
        this.glSelectPerfMonitorCountersAMD = caps.get(1101);
        this.glBeginPerfMonitorAMD = caps.get(1102);
        this.glEndPerfMonitorAMD = caps.get(1103);
        this.glGetPerfMonitorCounterDataAMD = caps.get(1104);
        this.glSetMultisamplefvAMD = caps.get(1105);
        this.glTexStorageSparseAMD = caps.get(1106);
        this.glTextureStorageSparseAMD = caps.get(1107);
        this.glStencilOpValueAMD = caps.get(1108);
        this.glTessellationFactorAMD = caps.get(1109);
        this.glTessellationModeAMD = caps.get(1110);
        this.glGetTextureHandleARB = caps.get(1111);
        this.glGetTextureSamplerHandleARB = caps.get(1112);
        this.glMakeTextureHandleResidentARB = caps.get(1113);
        this.glMakeTextureHandleNonResidentARB = caps.get(1114);
        this.glGetImageHandleARB = caps.get(1115);
        this.glMakeImageHandleResidentARB = caps.get(1116);
        this.glMakeImageHandleNonResidentARB = caps.get(1117);
        this.glUniformHandleui64ARB = caps.get(1118);
        this.glUniformHandleui64vARB = caps.get(1119);
        this.glProgramUniformHandleui64ARB = caps.get(1120);
        this.glProgramUniformHandleui64vARB = caps.get(1121);
        this.glIsTextureHandleResidentARB = caps.get(1122);
        this.glIsImageHandleResidentARB = caps.get(1123);
        this.glVertexAttribL1ui64ARB = caps.get(1124);
        this.glVertexAttribL1ui64vARB = caps.get(1125);
        this.glGetVertexAttribLui64vARB = caps.get(1126);
        this.glNamedBufferStorageEXT = caps.get(1127);
        this.glCreateSyncFromCLeventARB = caps.get(1128);
        this.glClearNamedBufferDataEXT = caps.get(1129);
        this.glClearNamedBufferSubDataEXT = caps.get(1130);
        this.glClampColorARB = caps.get(1131);
        this.glDispatchComputeGroupSizeARB = caps.get(1132);
        this.glDebugMessageControlARB = caps.get(1133);
        this.glDebugMessageInsertARB = caps.get(1134);
        this.glDebugMessageCallbackARB = caps.get(1135);
        this.glGetDebugMessageLogARB = caps.get(1136);
        this.glDrawBuffersARB = caps.get(1137);
        this.glBlendEquationiARB = caps.get(1138);
        this.glBlendEquationSeparateiARB = caps.get(1139);
        this.glBlendFunciARB = caps.get(1140);
        this.glBlendFuncSeparateiARB = caps.get(1141);
        this.glDrawArraysInstancedARB = caps.get(1142);
        this.glDrawElementsInstancedARB = caps.get(1143);
        this.glPrimitiveBoundingBoxARB = caps.get(1144);
        this.glNamedFramebufferParameteriEXT = caps.get(1145);
        this.glGetNamedFramebufferParameterivEXT = caps.get(1146);
        this.glProgramParameteriARB = caps.get(1147);
        this.glFramebufferTextureARB = caps.get(1148);
        this.glFramebufferTextureLayerARB = caps.get(1149);
        this.glFramebufferTextureFaceARB = caps.get(1150);
        this.glSpecializeShaderARB = caps.get(1151);
        this.glProgramUniform1dEXT = caps.get(1152);
        this.glProgramUniform2dEXT = caps.get(1153);
        this.glProgramUniform3dEXT = caps.get(1154);
        this.glProgramUniform4dEXT = caps.get(1155);
        this.glProgramUniform1dvEXT = caps.get(1156);
        this.glProgramUniform2dvEXT = caps.get(1157);
        this.glProgramUniform3dvEXT = caps.get(1158);
        this.glProgramUniform4dvEXT = caps.get(1159);
        this.glProgramUniformMatrix2dvEXT = caps.get(1160);
        this.glProgramUniformMatrix3dvEXT = caps.get(1161);
        this.glProgramUniformMatrix4dvEXT = caps.get(1162);
        this.glProgramUniformMatrix2x3dvEXT = caps.get(1163);
        this.glProgramUniformMatrix2x4dvEXT = caps.get(1164);
        this.glProgramUniformMatrix3x2dvEXT = caps.get(1165);
        this.glProgramUniformMatrix3x4dvEXT = caps.get(1166);
        this.glProgramUniformMatrix4x2dvEXT = caps.get(1167);
        this.glProgramUniformMatrix4x3dvEXT = caps.get(1168);
        this.glUniform1i64ARB = caps.get(1169);
        this.glUniform1i64vARB = caps.get(1170);
        this.glProgramUniform1i64ARB = caps.get(1171);
        this.glProgramUniform1i64vARB = caps.get(1172);
        this.glUniform2i64ARB = caps.get(1173);
        this.glUniform2i64vARB = caps.get(1174);
        this.glProgramUniform2i64ARB = caps.get(1175);
        this.glProgramUniform2i64vARB = caps.get(1176);
        this.glUniform3i64ARB = caps.get(1177);
        this.glUniform3i64vARB = caps.get(1178);
        this.glProgramUniform3i64ARB = caps.get(1179);
        this.glProgramUniform3i64vARB = caps.get(1180);
        this.glUniform4i64ARB = caps.get(1181);
        this.glUniform4i64vARB = caps.get(1182);
        this.glProgramUniform4i64ARB = caps.get(1183);
        this.glProgramUniform4i64vARB = caps.get(1184);
        this.glUniform1ui64ARB = caps.get(1185);
        this.glUniform1ui64vARB = caps.get(1186);
        this.glProgramUniform1ui64ARB = caps.get(1187);
        this.glProgramUniform1ui64vARB = caps.get(1188);
        this.glUniform2ui64ARB = caps.get(1189);
        this.glUniform2ui64vARB = caps.get(1190);
        this.glProgramUniform2ui64ARB = caps.get(1191);
        this.glProgramUniform2ui64vARB = caps.get(1192);
        this.glUniform3ui64ARB = caps.get(1193);
        this.glUniform3ui64vARB = caps.get(1194);
        this.glProgramUniform3ui64ARB = caps.get(1195);
        this.glProgramUniform3ui64vARB = caps.get(1196);
        this.glUniform4ui64ARB = caps.get(1197);
        this.glUniform4ui64vARB = caps.get(1198);
        this.glProgramUniform4ui64ARB = caps.get(1199);
        this.glProgramUniform4ui64vARB = caps.get(1200);
        this.glGetUniformi64vARB = caps.get(1201);
        this.glGetUniformui64vARB = caps.get(1202);
        this.glGetnUniformi64vARB = caps.get(1203);
        this.glGetnUniformui64vARB = caps.get(1204);
        this.glColorTable = caps.get(1205);
        this.glCopyColorTable = caps.get(1206);
        this.glColorTableParameteriv = caps.get(1207);
        this.glColorTableParameterfv = caps.get(1208);
        this.glGetColorTable = caps.get(1209);
        this.glGetColorTableParameteriv = caps.get(1210);
        this.glGetColorTableParameterfv = caps.get(1211);
        this.glColorSubTable = caps.get(1212);
        this.glCopyColorSubTable = caps.get(1213);
        this.glConvolutionFilter1D = caps.get(1214);
        this.glConvolutionFilter2D = caps.get(1215);
        this.glCopyConvolutionFilter1D = caps.get(1216);
        this.glCopyConvolutionFilter2D = caps.get(1217);
        this.glGetConvolutionFilter = caps.get(1218);
        this.glSeparableFilter2D = caps.get(1219);
        this.glGetSeparableFilter = caps.get(1220);
        this.glConvolutionParameteri = caps.get(1221);
        this.glConvolutionParameteriv = caps.get(1222);
        this.glConvolutionParameterf = caps.get(1223);
        this.glConvolutionParameterfv = caps.get(1224);
        this.glGetConvolutionParameteriv = caps.get(1225);
        this.glGetConvolutionParameterfv = caps.get(1226);
        this.glHistogram = caps.get(1227);
        this.glResetHistogram = caps.get(1228);
        this.glGetHistogram = caps.get(1229);
        this.glGetHistogramParameteriv = caps.get(1230);
        this.glGetHistogramParameterfv = caps.get(1231);
        this.glMinmax = caps.get(1232);
        this.glResetMinmax = caps.get(1233);
        this.glGetMinmax = caps.get(1234);
        this.glGetMinmaxParameteriv = caps.get(1235);
        this.glGetMinmaxParameterfv = caps.get(1236);
        this.glMultiDrawArraysIndirectCountARB = caps.get(1237);
        this.glMultiDrawElementsIndirectCountARB = caps.get(1238);
        this.glVertexAttribDivisorARB = caps.get(1239);
        this.glVertexArrayVertexAttribDivisorEXT = caps.get(1240);
        this.glCurrentPaletteMatrixARB = caps.get(1241);
        this.glMatrixIndexuivARB = caps.get(1242);
        this.glMatrixIndexubvARB = caps.get(1243);
        this.glMatrixIndexusvARB = caps.get(1244);
        this.glMatrixIndexPointerARB = caps.get(1245);
        this.glSampleCoverageARB = caps.get(1246);
        this.glActiveTextureARB = caps.get(1247);
        this.glClientActiveTextureARB = caps.get(1248);
        this.glMultiTexCoord1fARB = caps.get(1249);
        this.glMultiTexCoord1sARB = caps.get(1250);
        this.glMultiTexCoord1iARB = caps.get(1251);
        this.glMultiTexCoord1dARB = caps.get(1252);
        this.glMultiTexCoord1fvARB = caps.get(1253);
        this.glMultiTexCoord1svARB = caps.get(1254);
        this.glMultiTexCoord1ivARB = caps.get(1255);
        this.glMultiTexCoord1dvARB = caps.get(1256);
        this.glMultiTexCoord2fARB = caps.get(1257);
        this.glMultiTexCoord2sARB = caps.get(1258);
        this.glMultiTexCoord2iARB = caps.get(1259);
        this.glMultiTexCoord2dARB = caps.get(1260);
        this.glMultiTexCoord2fvARB = caps.get(1261);
        this.glMultiTexCoord2svARB = caps.get(1262);
        this.glMultiTexCoord2ivARB = caps.get(1263);
        this.glMultiTexCoord2dvARB = caps.get(1264);
        this.glMultiTexCoord3fARB = caps.get(1265);
        this.glMultiTexCoord3sARB = caps.get(1266);
        this.glMultiTexCoord3iARB = caps.get(1267);
        this.glMultiTexCoord3dARB = caps.get(1268);
        this.glMultiTexCoord3fvARB = caps.get(1269);
        this.glMultiTexCoord3svARB = caps.get(1270);
        this.glMultiTexCoord3ivARB = caps.get(1271);
        this.glMultiTexCoord3dvARB = caps.get(1272);
        this.glMultiTexCoord4fARB = caps.get(1273);
        this.glMultiTexCoord4sARB = caps.get(1274);
        this.glMultiTexCoord4iARB = caps.get(1275);
        this.glMultiTexCoord4dARB = caps.get(1276);
        this.glMultiTexCoord4fvARB = caps.get(1277);
        this.glMultiTexCoord4svARB = caps.get(1278);
        this.glMultiTexCoord4ivARB = caps.get(1279);
        this.glMultiTexCoord4dvARB = caps.get(1280);
        this.glGenQueriesARB = caps.get(1281);
        this.glDeleteQueriesARB = caps.get(1282);
        this.glIsQueryARB = caps.get(1283);
        this.glBeginQueryARB = caps.get(1284);
        this.glEndQueryARB = caps.get(1285);
        this.glGetQueryivARB = caps.get(1286);
        this.glGetQueryObjectivARB = caps.get(1287);
        this.glGetQueryObjectuivARB = caps.get(1288);
        this.glMaxShaderCompilerThreadsARB = caps.get(1289);
        this.glPointParameterfARB = caps.get(1290);
        this.glPointParameterfvARB = caps.get(1291);
        this.glGetGraphicsResetStatusARB = caps.get(1292);
        this.glGetnMapdvARB = caps.get(1293);
        this.glGetnMapfvARB = caps.get(1294);
        this.glGetnMapivARB = caps.get(1295);
        this.glGetnPixelMapfvARB = caps.get(1296);
        this.glGetnPixelMapuivARB = caps.get(1297);
        this.glGetnPixelMapusvARB = caps.get(1298);
        this.glGetnPolygonStippleARB = caps.get(1299);
        this.glGetnTexImageARB = caps.get(1300);
        this.glReadnPixelsARB = caps.get(1301);
        this.glGetnColorTableARB = caps.get(1302);
        this.glGetnConvolutionFilterARB = caps.get(1303);
        this.glGetnSeparableFilterARB = caps.get(1304);
        this.glGetnHistogramARB = caps.get(1305);
        this.glGetnMinmaxARB = caps.get(1306);
        this.glGetnCompressedTexImageARB = caps.get(1307);
        this.glGetnUniformfvARB = caps.get(1308);
        this.glGetnUniformivARB = caps.get(1309);
        this.glGetnUniformuivARB = caps.get(1310);
        this.glGetnUniformdvARB = caps.get(1311);
        this.glFramebufferSampleLocationsfvARB = caps.get(1312);
        this.glNamedFramebufferSampleLocationsfvARB = caps.get(1313);
        this.glEvaluateDepthValuesARB = caps.get(1314);
        this.glMinSampleShadingARB = caps.get(1315);
        this.glDeleteObjectARB = caps.get(1316);
        this.glGetHandleARB = caps.get(1317);
        this.glDetachObjectARB = caps.get(1318);
        this.glCreateShaderObjectARB = caps.get(1319);
        this.glShaderSourceARB = caps.get(1320);
        this.glCompileShaderARB = caps.get(1321);
        this.glCreateProgramObjectARB = caps.get(1322);
        this.glAttachObjectARB = caps.get(1323);
        this.glLinkProgramARB = caps.get(1324);
        this.glUseProgramObjectARB = caps.get(1325);
        this.glValidateProgramARB = caps.get(1326);
        this.glUniform1fARB = caps.get(1327);
        this.glUniform2fARB = caps.get(1328);
        this.glUniform3fARB = caps.get(1329);
        this.glUniform4fARB = caps.get(1330);
        this.glUniform1iARB = caps.get(1331);
        this.glUniform2iARB = caps.get(1332);
        this.glUniform3iARB = caps.get(1333);
        this.glUniform4iARB = caps.get(1334);
        this.glUniform1fvARB = caps.get(1335);
        this.glUniform2fvARB = caps.get(1336);
        this.glUniform3fvARB = caps.get(1337);
        this.glUniform4fvARB = caps.get(1338);
        this.glUniform1ivARB = caps.get(1339);
        this.glUniform2ivARB = caps.get(1340);
        this.glUniform3ivARB = caps.get(1341);
        this.glUniform4ivARB = caps.get(1342);
        this.glUniformMatrix2fvARB = caps.get(1343);
        this.glUniformMatrix3fvARB = caps.get(1344);
        this.glUniformMatrix4fvARB = caps.get(1345);
        this.glGetObjectParameterfvARB = caps.get(1346);
        this.glGetObjectParameterivARB = caps.get(1347);
        this.glGetInfoLogARB = caps.get(1348);
        this.glGetAttachedObjectsARB = caps.get(1349);
        this.glGetUniformLocationARB = caps.get(1350);
        this.glGetActiveUniformARB = caps.get(1351);
        this.glGetUniformfvARB = caps.get(1352);
        this.glGetUniformivARB = caps.get(1353);
        this.glGetShaderSourceARB = caps.get(1354);
        this.glNamedStringARB = caps.get(1355);
        this.glDeleteNamedStringARB = caps.get(1356);
        this.glCompileShaderIncludeARB = caps.get(1357);
        this.glIsNamedStringARB = caps.get(1358);
        this.glGetNamedStringARB = caps.get(1359);
        this.glGetNamedStringivARB = caps.get(1360);
        this.glBufferPageCommitmentARB = caps.get(1361);
        this.glNamedBufferPageCommitmentEXT = caps.get(1362);
        this.glNamedBufferPageCommitmentARB = caps.get(1363);
        this.glTexPageCommitmentARB = caps.get(1364);
        this.glTexturePageCommitmentEXT = caps.get(1365);
        this.glTexBufferARB = caps.get(1366);
        this.glTextureBufferRangeEXT = caps.get(1367);
        this.glCompressedTexImage3DARB = caps.get(1368);
        this.glCompressedTexImage2DARB = caps.get(1369);
        this.glCompressedTexImage1DARB = caps.get(1370);
        this.glCompressedTexSubImage3DARB = caps.get(1371);
        this.glCompressedTexSubImage2DARB = caps.get(1372);
        this.glCompressedTexSubImage1DARB = caps.get(1373);
        this.glGetCompressedTexImageARB = caps.get(1374);
        this.glTextureStorage1DEXT = caps.get(1375);
        this.glTextureStorage2DEXT = caps.get(1376);
        this.glTextureStorage3DEXT = caps.get(1377);
        this.glTextureStorage2DMultisampleEXT = caps.get(1378);
        this.glTextureStorage3DMultisampleEXT = caps.get(1379);
        this.glLoadTransposeMatrixfARB = caps.get(1380);
        this.glLoadTransposeMatrixdARB = caps.get(1381);
        this.glMultTransposeMatrixfARB = caps.get(1382);
        this.glMultTransposeMatrixdARB = caps.get(1383);
        this.glVertexArrayVertexAttribLOffsetEXT = caps.get(1384);
        this.glVertexArrayBindVertexBufferEXT = caps.get(1385);
        this.glVertexArrayVertexAttribFormatEXT = caps.get(1386);
        this.glVertexArrayVertexAttribIFormatEXT = caps.get(1387);
        this.glVertexArrayVertexAttribLFormatEXT = caps.get(1388);
        this.glVertexArrayVertexAttribBindingEXT = caps.get(1389);
        this.glVertexArrayVertexBindingDivisorEXT = caps.get(1390);
        this.glWeightfvARB = caps.get(1391);
        this.glWeightbvARB = caps.get(1392);
        this.glWeightubvARB = caps.get(1393);
        this.glWeightsvARB = caps.get(1394);
        this.glWeightusvARB = caps.get(1395);
        this.glWeightivARB = caps.get(1396);
        this.glWeightuivARB = caps.get(1397);
        this.glWeightdvARB = caps.get(1398);
        this.glWeightPointerARB = caps.get(1399);
        this.glVertexBlendARB = caps.get(1400);
        this.glBindBufferARB = caps.get(1401);
        this.glDeleteBuffersARB = caps.get(1402);
        this.glGenBuffersARB = caps.get(1403);
        this.glIsBufferARB = caps.get(1404);
        this.glBufferDataARB = caps.get(1405);
        this.glBufferSubDataARB = caps.get(1406);
        this.glGetBufferSubDataARB = caps.get(1407);
        this.glMapBufferARB = caps.get(1408);
        this.glUnmapBufferARB = caps.get(1409);
        this.glGetBufferParameterivARB = caps.get(1410);
        this.glGetBufferPointervARB = caps.get(1411);
        this.glVertexAttrib1sARB = caps.get(1412);
        this.glVertexAttrib1fARB = caps.get(1413);
        this.glVertexAttrib1dARB = caps.get(1414);
        this.glVertexAttrib2sARB = caps.get(1415);
        this.glVertexAttrib2fARB = caps.get(1416);
        this.glVertexAttrib2dARB = caps.get(1417);
        this.glVertexAttrib3sARB = caps.get(1418);
        this.glVertexAttrib3fARB = caps.get(1419);
        this.glVertexAttrib3dARB = caps.get(1420);
        this.glVertexAttrib4sARB = caps.get(1421);
        this.glVertexAttrib4fARB = caps.get(1422);
        this.glVertexAttrib4dARB = caps.get(1423);
        this.glVertexAttrib4NubARB = caps.get(1424);
        this.glVertexAttrib1svARB = caps.get(1425);
        this.glVertexAttrib1fvARB = caps.get(1426);
        this.glVertexAttrib1dvARB = caps.get(1427);
        this.glVertexAttrib2svARB = caps.get(1428);
        this.glVertexAttrib2fvARB = caps.get(1429);
        this.glVertexAttrib2dvARB = caps.get(1430);
        this.glVertexAttrib3svARB = caps.get(1431);
        this.glVertexAttrib3fvARB = caps.get(1432);
        this.glVertexAttrib3dvARB = caps.get(1433);
        this.glVertexAttrib4fvARB = caps.get(1434);
        this.glVertexAttrib4bvARB = caps.get(1435);
        this.glVertexAttrib4svARB = caps.get(1436);
        this.glVertexAttrib4ivARB = caps.get(1437);
        this.glVertexAttrib4ubvARB = caps.get(1438);
        this.glVertexAttrib4usvARB = caps.get(1439);
        this.glVertexAttrib4uivARB = caps.get(1440);
        this.glVertexAttrib4dvARB = caps.get(1441);
        this.glVertexAttrib4NbvARB = caps.get(1442);
        this.glVertexAttrib4NsvARB = caps.get(1443);
        this.glVertexAttrib4NivARB = caps.get(1444);
        this.glVertexAttrib4NubvARB = caps.get(1445);
        this.glVertexAttrib4NusvARB = caps.get(1446);
        this.glVertexAttrib4NuivARB = caps.get(1447);
        this.glVertexAttribPointerARB = caps.get(1448);
        this.glEnableVertexAttribArrayARB = caps.get(1449);
        this.glDisableVertexAttribArrayARB = caps.get(1450);
        this.glProgramStringARB = caps.get(1451);
        this.glBindProgramARB = caps.get(1452);
        this.glDeleteProgramsARB = caps.get(1453);
        this.glGenProgramsARB = caps.get(1454);
        this.glProgramEnvParameter4dARB = caps.get(1455);
        this.glProgramEnvParameter4dvARB = caps.get(1456);
        this.glProgramEnvParameter4fARB = caps.get(1457);
        this.glProgramEnvParameter4fvARB = caps.get(1458);
        this.glProgramLocalParameter4dARB = caps.get(1459);
        this.glProgramLocalParameter4dvARB = caps.get(1460);
        this.glProgramLocalParameter4fARB = caps.get(1461);
        this.glProgramLocalParameter4fvARB = caps.get(1462);
        this.glGetProgramEnvParameterfvARB = caps.get(1463);
        this.glGetProgramEnvParameterdvARB = caps.get(1464);
        this.glGetProgramLocalParameterfvARB = caps.get(1465);
        this.glGetProgramLocalParameterdvARB = caps.get(1466);
        this.glGetProgramivARB = caps.get(1467);
        this.glGetProgramStringARB = caps.get(1468);
        this.glGetVertexAttribfvARB = caps.get(1469);
        this.glGetVertexAttribdvARB = caps.get(1470);
        this.glGetVertexAttribivARB = caps.get(1471);
        this.glGetVertexAttribPointervARB = caps.get(1472);
        this.glIsProgramARB = caps.get(1473);
        this.glBindAttribLocationARB = caps.get(1474);
        this.glGetActiveAttribARB = caps.get(1475);
        this.glGetAttribLocationARB = caps.get(1476);
        this.glWindowPos2iARB = caps.get(1477);
        this.glWindowPos2sARB = caps.get(1478);
        this.glWindowPos2fARB = caps.get(1479);
        this.glWindowPos2dARB = caps.get(1480);
        this.glWindowPos2ivARB = caps.get(1481);
        this.glWindowPos2svARB = caps.get(1482);
        this.glWindowPos2fvARB = caps.get(1483);
        this.glWindowPos2dvARB = caps.get(1484);
        this.glWindowPos3iARB = caps.get(1485);
        this.glWindowPos3sARB = caps.get(1486);
        this.glWindowPos3fARB = caps.get(1487);
        this.glWindowPos3dARB = caps.get(1488);
        this.glWindowPos3ivARB = caps.get(1489);
        this.glWindowPos3svARB = caps.get(1490);
        this.glWindowPos3fvARB = caps.get(1491);
        this.glWindowPos3dvARB = caps.get(1492);
        this.glUniformBufferEXT = caps.get(1493);
        this.glGetUniformBufferSizeEXT = caps.get(1494);
        this.glGetUniformOffsetEXT = caps.get(1495);
        this.glBlendColorEXT = caps.get(1496);
        this.glBlendEquationSeparateEXT = caps.get(1497);
        this.glBlendFuncSeparateEXT = caps.get(1498);
        this.glBlendEquationEXT = caps.get(1499);
        this.glLockArraysEXT = caps.get(1500);
        this.glUnlockArraysEXT = caps.get(1501);
        this.glLabelObjectEXT = caps.get(1502);
        this.glGetObjectLabelEXT = caps.get(1503);
        this.glInsertEventMarkerEXT = caps.get(1504);
        this.glPushGroupMarkerEXT = caps.get(1505);
        this.glPopGroupMarkerEXT = caps.get(1506);
        this.glDepthBoundsEXT = caps.get(1507);
        this.glClientAttribDefaultEXT = caps.get(1508);
        this.glPushClientAttribDefaultEXT = caps.get(1509);
        this.glMatrixLoadfEXT = caps.get(1510);
        this.glMatrixLoaddEXT = caps.get(1511);
        this.glMatrixMultfEXT = caps.get(1512);
        this.glMatrixMultdEXT = caps.get(1513);
        this.glMatrixLoadIdentityEXT = caps.get(1514);
        this.glMatrixRotatefEXT = caps.get(1515);
        this.glMatrixRotatedEXT = caps.get(1516);
        this.glMatrixScalefEXT = caps.get(1517);
        this.glMatrixScaledEXT = caps.get(1518);
        this.glMatrixTranslatefEXT = caps.get(1519);
        this.glMatrixTranslatedEXT = caps.get(1520);
        this.glMatrixOrthoEXT = caps.get(1521);
        this.glMatrixFrustumEXT = caps.get(1522);
        this.glMatrixPushEXT = caps.get(1523);
        this.glMatrixPopEXT = caps.get(1524);
        this.glTextureParameteriEXT = caps.get(1525);
        this.glTextureParameterivEXT = caps.get(1526);
        this.glTextureParameterfEXT = caps.get(1527);
        this.glTextureParameterfvEXT = caps.get(1528);
        this.glTextureImage1DEXT = caps.get(1529);
        this.glTextureImage2DEXT = caps.get(1530);
        this.glTextureSubImage1DEXT = caps.get(1531);
        this.glTextureSubImage2DEXT = caps.get(1532);
        this.glCopyTextureImage1DEXT = caps.get(1533);
        this.glCopyTextureImage2DEXT = caps.get(1534);
        this.glCopyTextureSubImage1DEXT = caps.get(1535);
        this.glCopyTextureSubImage2DEXT = caps.get(1536);
        this.glGetTextureImageEXT = caps.get(1537);
        this.glGetTextureParameterfvEXT = caps.get(1538);
        this.glGetTextureParameterivEXT = caps.get(1539);
        this.glGetTextureLevelParameterfvEXT = caps.get(1540);
        this.glGetTextureLevelParameterivEXT = caps.get(1541);
        this.glTextureImage3DEXT = caps.get(1542);
        this.glTextureSubImage3DEXT = caps.get(1543);
        this.glCopyTextureSubImage3DEXT = caps.get(1544);
        this.glBindMultiTextureEXT = caps.get(1545);
        this.glMultiTexCoordPointerEXT = caps.get(1546);
        this.glMultiTexEnvfEXT = caps.get(1547);
        this.glMultiTexEnvfvEXT = caps.get(1548);
        this.glMultiTexEnviEXT = caps.get(1549);
        this.glMultiTexEnvivEXT = caps.get(1550);
        this.glMultiTexGendEXT = caps.get(1551);
        this.glMultiTexGendvEXT = caps.get(1552);
        this.glMultiTexGenfEXT = caps.get(1553);
        this.glMultiTexGenfvEXT = caps.get(1554);
        this.glMultiTexGeniEXT = caps.get(1555);
        this.glMultiTexGenivEXT = caps.get(1556);
        this.glGetMultiTexEnvfvEXT = caps.get(1557);
        this.glGetMultiTexEnvivEXT = caps.get(1558);
        this.glGetMultiTexGendvEXT = caps.get(1559);
        this.glGetMultiTexGenfvEXT = caps.get(1560);
        this.glGetMultiTexGenivEXT = caps.get(1561);
        this.glMultiTexParameteriEXT = caps.get(1562);
        this.glMultiTexParameterivEXT = caps.get(1563);
        this.glMultiTexParameterfEXT = caps.get(1564);
        this.glMultiTexParameterfvEXT = caps.get(1565);
        this.glMultiTexImage1DEXT = caps.get(1566);
        this.glMultiTexImage2DEXT = caps.get(1567);
        this.glMultiTexSubImage1DEXT = caps.get(1568);
        this.glMultiTexSubImage2DEXT = caps.get(1569);
        this.glCopyMultiTexImage1DEXT = caps.get(1570);
        this.glCopyMultiTexImage2DEXT = caps.get(1571);
        this.glCopyMultiTexSubImage1DEXT = caps.get(1572);
        this.glCopyMultiTexSubImage2DEXT = caps.get(1573);
        this.glGetMultiTexImageEXT = caps.get(1574);
        this.glGetMultiTexParameterfvEXT = caps.get(1575);
        this.glGetMultiTexParameterivEXT = caps.get(1576);
        this.glGetMultiTexLevelParameterfvEXT = caps.get(1577);
        this.glGetMultiTexLevelParameterivEXT = caps.get(1578);
        this.glMultiTexImage3DEXT = caps.get(1579);
        this.glMultiTexSubImage3DEXT = caps.get(1580);
        this.glCopyMultiTexSubImage3DEXT = caps.get(1581);
        this.glEnableClientStateIndexedEXT = caps.get(1582);
        this.glDisableClientStateIndexedEXT = caps.get(1583);
        this.glEnableClientStateiEXT = caps.get(1584);
        this.glDisableClientStateiEXT = caps.get(1585);
        this.glGetFloatIndexedvEXT = caps.get(1586);
        this.glGetDoubleIndexedvEXT = caps.get(1587);
        this.glGetPointerIndexedvEXT = caps.get(1588);
        this.glGetFloati_vEXT = caps.get(1589);
        this.glGetDoublei_vEXT = caps.get(1590);
        this.glGetPointeri_vEXT = caps.get(1591);
        this.glEnableIndexedEXT = caps.get(1592);
        this.glDisableIndexedEXT = caps.get(1593);
        this.glIsEnabledIndexedEXT = caps.get(1594);
        this.glGetIntegerIndexedvEXT = caps.get(1595);
        this.glGetBooleanIndexedvEXT = caps.get(1596);
        this.glNamedProgramStringEXT = caps.get(1597);
        this.glNamedProgramLocalParameter4dEXT = caps.get(1598);
        this.glNamedProgramLocalParameter4dvEXT = caps.get(1599);
        this.glNamedProgramLocalParameter4fEXT = caps.get(1600);
        this.glNamedProgramLocalParameter4fvEXT = caps.get(1601);
        this.glGetNamedProgramLocalParameterdvEXT = caps.get(1602);
        this.glGetNamedProgramLocalParameterfvEXT = caps.get(1603);
        this.glGetNamedProgramivEXT = caps.get(1604);
        this.glGetNamedProgramStringEXT = caps.get(1605);
        this.glCompressedTextureImage3DEXT = caps.get(1606);
        this.glCompressedTextureImage2DEXT = caps.get(1607);
        this.glCompressedTextureImage1DEXT = caps.get(1608);
        this.glCompressedTextureSubImage3DEXT = caps.get(1609);
        this.glCompressedTextureSubImage2DEXT = caps.get(1610);
        this.glCompressedTextureSubImage1DEXT = caps.get(1611);
        this.glGetCompressedTextureImageEXT = caps.get(1612);
        this.glCompressedMultiTexImage3DEXT = caps.get(1613);
        this.glCompressedMultiTexImage2DEXT = caps.get(1614);
        this.glCompressedMultiTexImage1DEXT = caps.get(1615);
        this.glCompressedMultiTexSubImage3DEXT = caps.get(1616);
        this.glCompressedMultiTexSubImage2DEXT = caps.get(1617);
        this.glCompressedMultiTexSubImage1DEXT = caps.get(1618);
        this.glGetCompressedMultiTexImageEXT = caps.get(1619);
        this.glMatrixLoadTransposefEXT = caps.get(1620);
        this.glMatrixLoadTransposedEXT = caps.get(1621);
        this.glMatrixMultTransposefEXT = caps.get(1622);
        this.glMatrixMultTransposedEXT = caps.get(1623);
        this.glNamedBufferDataEXT = caps.get(1624);
        this.glNamedBufferSubDataEXT = caps.get(1625);
        this.glMapNamedBufferEXT = caps.get(1626);
        this.glUnmapNamedBufferEXT = caps.get(1627);
        this.glGetNamedBufferParameterivEXT = caps.get(1628);
        this.glGetNamedBufferSubDataEXT = caps.get(1629);
        this.glProgramUniform1fEXT = caps.get(1630);
        this.glProgramUniform2fEXT = caps.get(1631);
        this.glProgramUniform3fEXT = caps.get(1632);
        this.glProgramUniform4fEXT = caps.get(1633);
        this.glProgramUniform1iEXT = caps.get(1634);
        this.glProgramUniform2iEXT = caps.get(1635);
        this.glProgramUniform3iEXT = caps.get(1636);
        this.glProgramUniform4iEXT = caps.get(1637);
        this.glProgramUniform1fvEXT = caps.get(1638);
        this.glProgramUniform2fvEXT = caps.get(1639);
        this.glProgramUniform3fvEXT = caps.get(1640);
        this.glProgramUniform4fvEXT = caps.get(1641);
        this.glProgramUniform1ivEXT = caps.get(1642);
        this.glProgramUniform2ivEXT = caps.get(1643);
        this.glProgramUniform3ivEXT = caps.get(1644);
        this.glProgramUniform4ivEXT = caps.get(1645);
        this.glProgramUniformMatrix2fvEXT = caps.get(1646);
        this.glProgramUniformMatrix3fvEXT = caps.get(1647);
        this.glProgramUniformMatrix4fvEXT = caps.get(1648);
        this.glProgramUniformMatrix2x3fvEXT = caps.get(1649);
        this.glProgramUniformMatrix3x2fvEXT = caps.get(1650);
        this.glProgramUniformMatrix2x4fvEXT = caps.get(1651);
        this.glProgramUniformMatrix4x2fvEXT = caps.get(1652);
        this.glProgramUniformMatrix3x4fvEXT = caps.get(1653);
        this.glProgramUniformMatrix4x3fvEXT = caps.get(1654);
        this.glTextureBufferEXT = caps.get(1655);
        this.glMultiTexBufferEXT = caps.get(1656);
        this.glTextureParameterIivEXT = caps.get(1657);
        this.glTextureParameterIuivEXT = caps.get(1658);
        this.glGetTextureParameterIivEXT = caps.get(1659);
        this.glGetTextureParameterIuivEXT = caps.get(1660);
        this.glMultiTexParameterIivEXT = caps.get(1661);
        this.glMultiTexParameterIuivEXT = caps.get(1662);
        this.glGetMultiTexParameterIivEXT = caps.get(1663);
        this.glGetMultiTexParameterIuivEXT = caps.get(1664);
        this.glProgramUniform1uiEXT = caps.get(1665);
        this.glProgramUniform2uiEXT = caps.get(1666);
        this.glProgramUniform3uiEXT = caps.get(1667);
        this.glProgramUniform4uiEXT = caps.get(1668);
        this.glProgramUniform1uivEXT = caps.get(1669);
        this.glProgramUniform2uivEXT = caps.get(1670);
        this.glProgramUniform3uivEXT = caps.get(1671);
        this.glProgramUniform4uivEXT = caps.get(1672);
        this.glNamedProgramLocalParameters4fvEXT = caps.get(1673);
        this.glNamedProgramLocalParameterI4iEXT = caps.get(1674);
        this.glNamedProgramLocalParameterI4ivEXT = caps.get(1675);
        this.glNamedProgramLocalParametersI4ivEXT = caps.get(1676);
        this.glNamedProgramLocalParameterI4uiEXT = caps.get(1677);
        this.glNamedProgramLocalParameterI4uivEXT = caps.get(1678);
        this.glNamedProgramLocalParametersI4uivEXT = caps.get(1679);
        this.glGetNamedProgramLocalParameterIivEXT = caps.get(1680);
        this.glGetNamedProgramLocalParameterIuivEXT = caps.get(1681);
        this.glNamedRenderbufferStorageEXT = caps.get(1682);
        this.glGetNamedRenderbufferParameterivEXT = caps.get(1683);
        this.glNamedRenderbufferStorageMultisampleEXT = caps.get(1684);
        this.glNamedRenderbufferStorageMultisampleCoverageEXT = caps.get(1685);
        this.glCheckNamedFramebufferStatusEXT = caps.get(1686);
        this.glNamedFramebufferTexture1DEXT = caps.get(1687);
        this.glNamedFramebufferTexture2DEXT = caps.get(1688);
        this.glNamedFramebufferTexture3DEXT = caps.get(1689);
        this.glNamedFramebufferRenderbufferEXT = caps.get(1690);
        this.glGetNamedFramebufferAttachmentParameterivEXT = caps.get(1691);
        this.glGenerateTextureMipmapEXT = caps.get(1692);
        this.glGenerateMultiTexMipmapEXT = caps.get(1693);
        this.glFramebufferDrawBufferEXT = caps.get(1694);
        this.glFramebufferDrawBuffersEXT = caps.get(1695);
        this.glFramebufferReadBufferEXT = caps.get(1696);
        this.glGetFramebufferParameterivEXT = caps.get(1697);
        this.glNamedCopyBufferSubDataEXT = caps.get(1698);
        this.glNamedFramebufferTextureEXT = caps.get(1699);
        this.glNamedFramebufferTextureLayerEXT = caps.get(1700);
        this.glNamedFramebufferTextureFaceEXT = caps.get(1701);
        this.glTextureRenderbufferEXT = caps.get(1702);
        this.glMultiTexRenderbufferEXT = caps.get(1703);
        this.glVertexArrayVertexOffsetEXT = caps.get(1704);
        this.glVertexArrayColorOffsetEXT = caps.get(1705);
        this.glVertexArrayEdgeFlagOffsetEXT = caps.get(1706);
        this.glVertexArrayIndexOffsetEXT = caps.get(1707);
        this.glVertexArrayNormalOffsetEXT = caps.get(1708);
        this.glVertexArrayTexCoordOffsetEXT = caps.get(1709);
        this.glVertexArrayMultiTexCoordOffsetEXT = caps.get(1710);
        this.glVertexArrayFogCoordOffsetEXT = caps.get(1711);
        this.glVertexArraySecondaryColorOffsetEXT = caps.get(1712);
        this.glVertexArrayVertexAttribOffsetEXT = caps.get(1713);
        this.glVertexArrayVertexAttribIOffsetEXT = caps.get(1714);
        this.glEnableVertexArrayEXT = caps.get(1715);
        this.glDisableVertexArrayEXT = caps.get(1716);
        this.glEnableVertexArrayAttribEXT = caps.get(1717);
        this.glDisableVertexArrayAttribEXT = caps.get(1718);
        this.glGetVertexArrayIntegervEXT = caps.get(1719);
        this.glGetVertexArrayPointervEXT = caps.get(1720);
        this.glGetVertexArrayIntegeri_vEXT = caps.get(1721);
        this.glGetVertexArrayPointeri_vEXT = caps.get(1722);
        this.glMapNamedBufferRangeEXT = caps.get(1723);
        this.glFlushMappedNamedBufferRangeEXT = caps.get(1724);
        this.glColorMaskIndexedEXT = caps.get(1725);
        this.glDrawArraysInstancedEXT = caps.get(1726);
        this.glDrawElementsInstancedEXT = caps.get(1727);
        this.glEGLImageTargetTexStorageEXT = caps.get(1728);
        this.glEGLImageTargetTextureStorageEXT = caps.get(1729);
        this.glBufferStorageExternalEXT = caps.get(1730);
        this.glNamedBufferStorageExternalEXT = caps.get(1731);
        this.glBlitFramebufferEXT = caps.get(1732);
        this.glBlitFramebufferLayersEXT = caps.get(1733);
        this.glBlitFramebufferLayerEXT = caps.get(1734);
        this.glRenderbufferStorageMultisampleEXT = caps.get(1735);
        this.glIsRenderbufferEXT = caps.get(1736);
        this.glBindRenderbufferEXT = caps.get(1737);
        this.glDeleteRenderbuffersEXT = caps.get(1738);
        this.glGenRenderbuffersEXT = caps.get(1739);
        this.glRenderbufferStorageEXT = caps.get(1740);
        this.glGetRenderbufferParameterivEXT = caps.get(1741);
        this.glIsFramebufferEXT = caps.get(1742);
        this.glBindFramebufferEXT = caps.get(1743);
        this.glDeleteFramebuffersEXT = caps.get(1744);
        this.glGenFramebuffersEXT = caps.get(1745);
        this.glCheckFramebufferStatusEXT = caps.get(1746);
        this.glFramebufferTexture1DEXT = caps.get(1747);
        this.glFramebufferTexture2DEXT = caps.get(1748);
        this.glFramebufferTexture3DEXT = caps.get(1749);
        this.glFramebufferRenderbufferEXT = caps.get(1750);
        this.glGetFramebufferAttachmentParameterivEXT = caps.get(1751);
        this.glGenerateMipmapEXT = caps.get(1752);
        this.glProgramParameteriEXT = caps.get(1753);
        this.glFramebufferTextureEXT = caps.get(1754);
        this.glFramebufferTextureLayerEXT = caps.get(1755);
        this.glFramebufferTextureFaceEXT = caps.get(1756);
        this.glProgramEnvParameters4fvEXT = caps.get(1757);
        this.glProgramLocalParameters4fvEXT = caps.get(1758);
        this.glVertexAttribI1iEXT = caps.get(1759);
        this.glVertexAttribI2iEXT = caps.get(1760);
        this.glVertexAttribI3iEXT = caps.get(1761);
        this.glVertexAttribI4iEXT = caps.get(1762);
        this.glVertexAttribI1uiEXT = caps.get(1763);
        this.glVertexAttribI2uiEXT = caps.get(1764);
        this.glVertexAttribI3uiEXT = caps.get(1765);
        this.glVertexAttribI4uiEXT = caps.get(1766);
        this.glVertexAttribI1ivEXT = caps.get(1767);
        this.glVertexAttribI2ivEXT = caps.get(1768);
        this.glVertexAttribI3ivEXT = caps.get(1769);
        this.glVertexAttribI4ivEXT = caps.get(1770);
        this.glVertexAttribI1uivEXT = caps.get(1771);
        this.glVertexAttribI2uivEXT = caps.get(1772);
        this.glVertexAttribI3uivEXT = caps.get(1773);
        this.glVertexAttribI4uivEXT = caps.get(1774);
        this.glVertexAttribI4bvEXT = caps.get(1775);
        this.glVertexAttribI4svEXT = caps.get(1776);
        this.glVertexAttribI4ubvEXT = caps.get(1777);
        this.glVertexAttribI4usvEXT = caps.get(1778);
        this.glVertexAttribIPointerEXT = caps.get(1779);
        this.glGetVertexAttribIivEXT = caps.get(1780);
        this.glGetVertexAttribIuivEXT = caps.get(1781);
        this.glGetUniformuivEXT = caps.get(1782);
        this.glBindFragDataLocationEXT = caps.get(1783);
        this.glGetFragDataLocationEXT = caps.get(1784);
        this.glUniform1uiEXT = caps.get(1785);
        this.glUniform2uiEXT = caps.get(1786);
        this.glUniform3uiEXT = caps.get(1787);
        this.glUniform4uiEXT = caps.get(1788);
        this.glUniform1uivEXT = caps.get(1789);
        this.glUniform2uivEXT = caps.get(1790);
        this.glUniform3uivEXT = caps.get(1791);
        this.glUniform4uivEXT = caps.get(1792);
        this.glGetUnsignedBytevEXT = caps.get(1793);
        this.glGetUnsignedBytei_vEXT = caps.get(1794);
        this.glDeleteMemoryObjectsEXT = caps.get(1795);
        this.glIsMemoryObjectEXT = caps.get(1796);
        this.glCreateMemoryObjectsEXT = caps.get(1797);
        this.glMemoryObjectParameterivEXT = caps.get(1798);
        this.glGetMemoryObjectParameterivEXT = caps.get(1799);
        this.glTexStorageMem2DEXT = caps.get(1800);
        this.glTexStorageMem2DMultisampleEXT = caps.get(1801);
        this.glTexStorageMem3DEXT = caps.get(1802);
        this.glTexStorageMem3DMultisampleEXT = caps.get(1803);
        this.glBufferStorageMemEXT = caps.get(1804);
        this.glTextureStorageMem2DEXT = caps.get(1805);
        this.glTextureStorageMem2DMultisampleEXT = caps.get(1806);
        this.glTextureStorageMem3DEXT = caps.get(1807);
        this.glTextureStorageMem3DMultisampleEXT = caps.get(1808);
        this.glNamedBufferStorageMemEXT = caps.get(1809);
        this.glTexStorageMem1DEXT = caps.get(1810);
        this.glTextureStorageMem1DEXT = caps.get(1811);
        this.glImportMemoryFdEXT = caps.get(1812);
        this.glImportMemoryWin32HandleEXT = caps.get(1813);
        this.glImportMemoryWin32NameEXT = caps.get(1814);
        this.glPointParameterfEXT = caps.get(1815);
        this.glPointParameterfvEXT = caps.get(1816);
        this.glPolygonOffsetClampEXT = caps.get(1817);
        this.glProvokingVertexEXT = caps.get(1818);
        this.glRasterSamplesEXT = caps.get(1819);
        this.glSecondaryColor3bEXT = caps.get(1820);
        this.glSecondaryColor3sEXT = caps.get(1821);
        this.glSecondaryColor3iEXT = caps.get(1822);
        this.glSecondaryColor3fEXT = caps.get(1823);
        this.glSecondaryColor3dEXT = caps.get(1824);
        this.glSecondaryColor3ubEXT = caps.get(1825);
        this.glSecondaryColor3usEXT = caps.get(1826);
        this.glSecondaryColor3uiEXT = caps.get(1827);
        this.glSecondaryColor3bvEXT = caps.get(1828);
        this.glSecondaryColor3svEXT = caps.get(1829);
        this.glSecondaryColor3ivEXT = caps.get(1830);
        this.glSecondaryColor3fvEXT = caps.get(1831);
        this.glSecondaryColor3dvEXT = caps.get(1832);
        this.glSecondaryColor3ubvEXT = caps.get(1833);
        this.glSecondaryColor3usvEXT = caps.get(1834);
        this.glSecondaryColor3uivEXT = caps.get(1835);
        this.glSecondaryColorPointerEXT = caps.get(1836);
        this.glGenSemaphoresEXT = caps.get(1837);
        this.glDeleteSemaphoresEXT = caps.get(1838);
        this.glIsSemaphoreEXT = caps.get(1839);
        this.glSemaphoreParameterui64vEXT = caps.get(1840);
        this.glGetSemaphoreParameterui64vEXT = caps.get(1841);
        this.glWaitSemaphoreEXT = caps.get(1842);
        this.glSignalSemaphoreEXT = caps.get(1843);
        this.glImportSemaphoreFdEXT = caps.get(1844);
        this.glImportSemaphoreWin32HandleEXT = caps.get(1845);
        this.glImportSemaphoreWin32NameEXT = caps.get(1846);
        this.glUseShaderProgramEXT = caps.get(1847);
        this.glActiveProgramEXT = caps.get(1848);
        this.glCreateShaderProgramEXT = caps.get(1849);
        this.glFramebufferFetchBarrierEXT = caps.get(1850);
        this.glBindImageTextureEXT = caps.get(1851);
        this.glMemoryBarrierEXT = caps.get(1852);
        this.glStencilClearTagEXT = caps.get(1853);
        this.glActiveStencilFaceEXT = caps.get(1854);
        this.glTexBufferEXT = caps.get(1855);
        this.glClearColorIiEXT = caps.get(1856);
        this.glClearColorIuiEXT = caps.get(1857);
        this.glTexParameterIivEXT = caps.get(1858);
        this.glTexParameterIuivEXT = caps.get(1859);
        this.glGetTexParameterIivEXT = caps.get(1860);
        this.glGetTexParameterIuivEXT = caps.get(1861);
        this.glTexStorage1DEXT = caps.get(1862);
        this.glTexStorage2DEXT = caps.get(1863);
        this.glTexStorage3DEXT = caps.get(1864);
        this.glGetQueryObjecti64vEXT = caps.get(1865);
        this.glGetQueryObjectui64vEXT = caps.get(1866);
        this.glBindBufferRangeEXT = caps.get(1867);
        this.glBindBufferOffsetEXT = caps.get(1868);
        this.glBindBufferBaseEXT = caps.get(1869);
        this.glBeginTransformFeedbackEXT = caps.get(1870);
        this.glEndTransformFeedbackEXT = caps.get(1871);
        this.glTransformFeedbackVaryingsEXT = caps.get(1872);
        this.glGetTransformFeedbackVaryingEXT = caps.get(1873);
        this.glVertexAttribL1dEXT = caps.get(1874);
        this.glVertexAttribL2dEXT = caps.get(1875);
        this.glVertexAttribL3dEXT = caps.get(1876);
        this.glVertexAttribL4dEXT = caps.get(1877);
        this.glVertexAttribL1dvEXT = caps.get(1878);
        this.glVertexAttribL2dvEXT = caps.get(1879);
        this.glVertexAttribL3dvEXT = caps.get(1880);
        this.glVertexAttribL4dvEXT = caps.get(1881);
        this.glVertexAttribLPointerEXT = caps.get(1882);
        this.glGetVertexAttribLdvEXT = caps.get(1883);
        this.glAcquireKeyedMutexWin32EXT = caps.get(1884);
        this.glReleaseKeyedMutexWin32EXT = caps.get(1885);
        this.glWindowRectanglesEXT = caps.get(1886);
        this.glImportSyncEXT = caps.get(1887);
        this.glFrameTerminatorGREMEDY = caps.get(1888);
        this.glStringMarkerGREMEDY = caps.get(1889);
        this.glApplyFramebufferAttachmentCMAAINTEL = caps.get(1890);
        this.glSyncTextureINTEL = caps.get(1891);
        this.glUnmapTexture2DINTEL = caps.get(1892);
        this.glMapTexture2DINTEL = caps.get(1893);
        this.glBeginPerfQueryINTEL = caps.get(1894);
        this.glCreatePerfQueryINTEL = caps.get(1895);
        this.glDeletePerfQueryINTEL = caps.get(1896);
        this.glEndPerfQueryINTEL = caps.get(1897);
        this.glGetFirstPerfQueryIdINTEL = caps.get(1898);
        this.glGetNextPerfQueryIdINTEL = caps.get(1899);
        this.glGetPerfCounterInfoINTEL = caps.get(1900);
        this.glGetPerfQueryDataINTEL = caps.get(1901);
        this.glGetPerfQueryIdByNameINTEL = caps.get(1902);
        this.glGetPerfQueryInfoINTEL = caps.get(1903);
        this.glBlendBarrierKHR = caps.get(1904);
        this.glMaxShaderCompilerThreadsKHR = caps.get(1905);
        this.glFramebufferParameteriMESA = caps.get(1906);
        this.glGetFramebufferParameterivMESA = caps.get(1907);
        this.glAlphaToCoverageDitherControlNV = caps.get(1908);
        this.glMultiDrawArraysIndirectBindlessNV = caps.get(1909);
        this.glMultiDrawElementsIndirectBindlessNV = caps.get(1910);
        this.glMultiDrawArraysIndirectBindlessCountNV = caps.get(1911);
        this.glMultiDrawElementsIndirectBindlessCountNV = caps.get(1912);
        this.glGetTextureHandleNV = caps.get(1913);
        this.glGetTextureSamplerHandleNV = caps.get(1914);
        this.glMakeTextureHandleResidentNV = caps.get(1915);
        this.glMakeTextureHandleNonResidentNV = caps.get(1916);
        this.glGetImageHandleNV = caps.get(1917);
        this.glMakeImageHandleResidentNV = caps.get(1918);
        this.glMakeImageHandleNonResidentNV = caps.get(1919);
        this.glUniformHandleui64NV = caps.get(1920);
        this.glUniformHandleui64vNV = caps.get(1921);
        this.glProgramUniformHandleui64NV = caps.get(1922);
        this.glProgramUniformHandleui64vNV = caps.get(1923);
        this.glIsTextureHandleResidentNV = caps.get(1924);
        this.glIsImageHandleResidentNV = caps.get(1925);
        this.glBlendParameteriNV = caps.get(1926);
        this.glBlendBarrierNV = caps.get(1927);
        this.glViewportPositionWScaleNV = caps.get(1928);
        this.glCreateStatesNV = caps.get(1929);
        this.glDeleteStatesNV = caps.get(1930);
        this.glIsStateNV = caps.get(1931);
        this.glStateCaptureNV = caps.get(1932);
        this.glGetCommandHeaderNV = caps.get(1933);
        this.glGetStageIndexNV = caps.get(1934);
        this.glDrawCommandsNV = caps.get(1935);
        this.glDrawCommandsAddressNV = caps.get(1936);
        this.glDrawCommandsStatesNV = caps.get(1937);
        this.glDrawCommandsStatesAddressNV = caps.get(1938);
        this.glCreateCommandListsNV = caps.get(1939);
        this.glDeleteCommandListsNV = caps.get(1940);
        this.glIsCommandListNV = caps.get(1941);
        this.glListDrawCommandsStatesClientNV = caps.get(1942);
        this.glCommandListSegmentsNV = caps.get(1943);
        this.glCompileCommandListNV = caps.get(1944);
        this.glCallCommandListNV = caps.get(1945);
        this.glBeginConditionalRenderNV = caps.get(1946);
        this.glEndConditionalRenderNV = caps.get(1947);
        this.glSubpixelPrecisionBiasNV = caps.get(1948);
        this.glConservativeRasterParameterfNV = caps.get(1949);
        this.glConservativeRasterParameteriNV = caps.get(1950);
        this.glCopyImageSubDataNV = caps.get(1951);
        this.glDepthRangedNV = caps.get(1952);
        this.glClearDepthdNV = caps.get(1953);
        this.glDepthBoundsdNV = caps.get(1954);
        this.glDrawTextureNV = caps.get(1955);
        this.glDrawVkImageNV = caps.get(1956);
        this.glGetVkProcAddrNV = caps.get(1957);
        this.glWaitVkSemaphoreNV = caps.get(1958);
        this.glSignalVkSemaphoreNV = caps.get(1959);
        this.glSignalVkFenceNV = caps.get(1960);
        this.glGetMultisamplefvNV = caps.get(1961);
        this.glSampleMaskIndexedNV = caps.get(1962);
        this.glTexRenderbufferNV = caps.get(1963);
        this.glDeleteFencesNV = caps.get(1964);
        this.glGenFencesNV = caps.get(1965);
        this.glIsFenceNV = caps.get(1966);
        this.glTestFenceNV = caps.get(1967);
        this.glGetFenceivNV = caps.get(1968);
        this.glFinishFenceNV = caps.get(1969);
        this.glSetFenceNV = caps.get(1970);
        this.glFragmentCoverageColorNV = caps.get(1971);
        this.glCoverageModulationTableNV = caps.get(1972);
        this.glGetCoverageModulationTableNV = caps.get(1973);
        this.glCoverageModulationNV = caps.get(1974);
        this.glRenderbufferStorageMultisampleCoverageNV = caps.get(1975);
        this.glRenderGpuMaskNV = caps.get(1976);
        this.glMulticastBufferSubDataNV = caps.get(1977);
        this.glMulticastCopyBufferSubDataNV = caps.get(1978);
        this.glMulticastCopyImageSubDataNV = caps.get(1979);
        this.glMulticastBlitFramebufferNV = caps.get(1980);
        this.glMulticastFramebufferSampleLocationsfvNV = caps.get(1981);
        this.glMulticastBarrierNV = caps.get(1982);
        this.glMulticastWaitSyncNV = caps.get(1983);
        this.glMulticastGetQueryObjectivNV = caps.get(1984);
        this.glMulticastGetQueryObjectuivNV = caps.get(1985);
        this.glMulticastGetQueryObjecti64vNV = caps.get(1986);
        this.glMulticastGetQueryObjectui64vNV = caps.get(1987);
        this.glVertex2hNV = caps.get(1988);
        this.glVertex2hvNV = caps.get(1989);
        this.glVertex3hNV = caps.get(1990);
        this.glVertex3hvNV = caps.get(1991);
        this.glVertex4hNV = caps.get(1992);
        this.glVertex4hvNV = caps.get(1993);
        this.glNormal3hNV = caps.get(1994);
        this.glNormal3hvNV = caps.get(1995);
        this.glColor3hNV = caps.get(1996);
        this.glColor3hvNV = caps.get(1997);
        this.glColor4hNV = caps.get(1998);
        this.glColor4hvNV = caps.get(1999);
        this.glTexCoord1hNV = caps.get(2000);
        this.glTexCoord1hvNV = caps.get(2001);
        this.glTexCoord2hNV = caps.get(2002);
        this.glTexCoord2hvNV = caps.get(2003);
        this.glTexCoord3hNV = caps.get(2004);
        this.glTexCoord3hvNV = caps.get(2005);
        this.glTexCoord4hNV = caps.get(2006);
        this.glTexCoord4hvNV = caps.get(2007);
        this.glMultiTexCoord1hNV = caps.get(2008);
        this.glMultiTexCoord1hvNV = caps.get(2009);
        this.glMultiTexCoord2hNV = caps.get(2010);
        this.glMultiTexCoord2hvNV = caps.get(2011);
        this.glMultiTexCoord3hNV = caps.get(2012);
        this.glMultiTexCoord3hvNV = caps.get(2013);
        this.glMultiTexCoord4hNV = caps.get(2014);
        this.glMultiTexCoord4hvNV = caps.get(2015);
        this.glFogCoordhNV = caps.get(2016);
        this.glFogCoordhvNV = caps.get(2017);
        this.glSecondaryColor3hNV = caps.get(2018);
        this.glSecondaryColor3hvNV = caps.get(2019);
        this.glVertexWeighthNV = caps.get(2020);
        this.glVertexWeighthvNV = caps.get(2021);
        this.glVertexAttrib1hNV = caps.get(2022);
        this.glVertexAttrib1hvNV = caps.get(2023);
        this.glVertexAttrib2hNV = caps.get(2024);
        this.glVertexAttrib2hvNV = caps.get(2025);
        this.glVertexAttrib3hNV = caps.get(2026);
        this.glVertexAttrib3hvNV = caps.get(2027);
        this.glVertexAttrib4hNV = caps.get(2028);
        this.glVertexAttrib4hvNV = caps.get(2029);
        this.glVertexAttribs1hvNV = caps.get(2030);
        this.glVertexAttribs2hvNV = caps.get(2031);
        this.glVertexAttribs3hvNV = caps.get(2032);
        this.glVertexAttribs4hvNV = caps.get(2033);
        this.glGetInternalformatSampleivNV = caps.get(2034);
        this.glGetMemoryObjectDetachedResourcesuivNV = caps.get(2035);
        this.glResetMemoryObjectParameterNV = caps.get(2036);
        this.glTexAttachMemoryNV = caps.get(2037);
        this.glBufferAttachMemoryNV = caps.get(2038);
        this.glTextureAttachMemoryNV = caps.get(2039);
        this.glNamedBufferAttachMemoryNV = caps.get(2040);
        this.glBufferPageCommitmentMemNV = caps.get(2041);
        this.glNamedBufferPageCommitmentMemNV = caps.get(2042);
        this.glTexPageCommitmentMemNV = caps.get(2043);
        this.glTexturePageCommitmentMemNV = caps.get(2044);
        this.glDrawMeshTasksNV = caps.get(2045);
        this.glDrawMeshTasksIndirectNV = caps.get(2046);
        this.glMultiDrawMeshTasksIndirectNV = caps.get(2047);
        this.glMultiDrawMeshTasksIndirectCountNV = caps.get(2048);
        this.glPathCommandsNV = caps.get(2049);
        this.glPathCoordsNV = caps.get(2050);
        this.glPathSubCommandsNV = caps.get(2051);
        this.glPathSubCoordsNV = caps.get(2052);
        this.glPathStringNV = caps.get(2053);
        this.glPathGlyphsNV = caps.get(2054);
        this.glPathGlyphRangeNV = caps.get(2055);
        this.glPathGlyphIndexArrayNV = caps.get(2056);
        this.glPathMemoryGlyphIndexArrayNV = caps.get(2057);
        this.glCopyPathNV = caps.get(2058);
        this.glWeightPathsNV = caps.get(2059);
        this.glInterpolatePathsNV = caps.get(2060);
        this.glTransformPathNV = caps.get(2061);
        this.glPathParameterivNV = caps.get(2062);
        this.glPathParameteriNV = caps.get(2063);
        this.glPathParameterfvNV = caps.get(2064);
        this.glPathParameterfNV = caps.get(2065);
        this.glPathDashArrayNV = caps.get(2066);
        this.glGenPathsNV = caps.get(2067);
        this.glDeletePathsNV = caps.get(2068);
        this.glIsPathNV = caps.get(2069);
        this.glPathStencilFuncNV = caps.get(2070);
        this.glPathStencilDepthOffsetNV = caps.get(2071);
        this.glStencilFillPathNV = caps.get(2072);
        this.glStencilStrokePathNV = caps.get(2073);
        this.glStencilFillPathInstancedNV = caps.get(2074);
        this.glStencilStrokePathInstancedNV = caps.get(2075);
        this.glPathCoverDepthFuncNV = caps.get(2076);
        this.glPathColorGenNV = caps.get(2077);
        this.glPathTexGenNV = caps.get(2078);
        this.glPathFogGenNV = caps.get(2079);
        this.glCoverFillPathNV = caps.get(2080);
        this.glCoverStrokePathNV = caps.get(2081);
        this.glCoverFillPathInstancedNV = caps.get(2082);
        this.glCoverStrokePathInstancedNV = caps.get(2083);
        this.glStencilThenCoverFillPathNV = caps.get(2084);
        this.glStencilThenCoverStrokePathNV = caps.get(2085);
        this.glStencilThenCoverFillPathInstancedNV = caps.get(2086);
        this.glStencilThenCoverStrokePathInstancedNV = caps.get(2087);
        this.glPathGlyphIndexRangeNV = caps.get(2088);
        this.glProgramPathFragmentInputGenNV = caps.get(2089);
        this.glGetPathParameterivNV = caps.get(2090);
        this.glGetPathParameterfvNV = caps.get(2091);
        this.glGetPathCommandsNV = caps.get(2092);
        this.glGetPathCoordsNV = caps.get(2093);
        this.glGetPathDashArrayNV = caps.get(2094);
        this.glGetPathMetricsNV = caps.get(2095);
        this.glGetPathMetricRangeNV = caps.get(2096);
        this.glGetPathSpacingNV = caps.get(2097);
        this.glGetPathColorGenivNV = caps.get(2098);
        this.glGetPathColorGenfvNV = caps.get(2099);
        this.glGetPathTexGenivNV = caps.get(2100);
        this.glGetPathTexGenfvNV = caps.get(2101);
        this.glIsPointInFillPathNV = caps.get(2102);
        this.glIsPointInStrokePathNV = caps.get(2103);
        this.glGetPathLengthNV = caps.get(2104);
        this.glPointAlongPathNV = caps.get(2105);
        this.glMatrixLoad3x2fNV = caps.get(2106);
        this.glMatrixLoad3x3fNV = caps.get(2107);
        this.glMatrixLoadTranspose3x3fNV = caps.get(2108);
        this.glMatrixMult3x2fNV = caps.get(2109);
        this.glMatrixMult3x3fNV = caps.get(2110);
        this.glMatrixMultTranspose3x3fNV = caps.get(2111);
        this.glGetProgramResourcefvNV = caps.get(2112);
        this.glPixelDataRangeNV = caps.get(2113);
        this.glFlushPixelDataRangeNV = caps.get(2114);
        this.glPointParameteriNV = caps.get(2115);
        this.glPointParameterivNV = caps.get(2116);
        this.glPrimitiveRestartNV = caps.get(2117);
        this.glPrimitiveRestartIndexNV = caps.get(2118);
        this.glQueryResourceNV = caps.get(2119);
        this.glGenQueryResourceTagNV = caps.get(2120);
        this.glDeleteQueryResourceTagNV = caps.get(2121);
        this.glQueryResourceTagNV = caps.get(2122);
        this.glFramebufferSampleLocationsfvNV = caps.get(2123);
        this.glNamedFramebufferSampleLocationsfvNV = caps.get(2124);
        this.glResolveDepthValuesNV = caps.get(2125);
        this.glScissorExclusiveArrayvNV = caps.get(2126);
        this.glScissorExclusiveNV = caps.get(2127);
        this.glMakeBufferResidentNV = caps.get(2128);
        this.glMakeBufferNonResidentNV = caps.get(2129);
        this.glIsBufferResidentNV = caps.get(2130);
        this.glMakeNamedBufferResidentNV = caps.get(2131);
        this.glMakeNamedBufferNonResidentNV = caps.get(2132);
        this.glIsNamedBufferResidentNV = caps.get(2133);
        this.glGetBufferParameterui64vNV = caps.get(2134);
        this.glGetNamedBufferParameterui64vNV = caps.get(2135);
        this.glGetIntegerui64vNV = caps.get(2136);
        this.glUniformui64NV = caps.get(2137);
        this.glUniformui64vNV = caps.get(2138);
        this.glProgramUniformui64NV = caps.get(2139);
        this.glProgramUniformui64vNV = caps.get(2140);
        this.glBindShadingRateImageNV = caps.get(2141);
        this.glShadingRateImagePaletteNV = caps.get(2142);
        this.glGetShadingRateImagePaletteNV = caps.get(2143);
        this.glShadingRateImageBarrierNV = caps.get(2144);
        this.glShadingRateSampleOrderNV = caps.get(2145);
        this.glShadingRateSampleOrderCustomNV = caps.get(2146);
        this.glGetShadingRateSampleLocationivNV = caps.get(2147);
        this.glTextureBarrierNV = caps.get(2148);
        this.glTexImage2DMultisampleCoverageNV = caps.get(2149);
        this.glTexImage3DMultisampleCoverageNV = caps.get(2150);
        this.glTextureImage2DMultisampleNV = caps.get(2151);
        this.glTextureImage3DMultisampleNV = caps.get(2152);
        this.glTextureImage2DMultisampleCoverageNV = caps.get(2153);
        this.glTextureImage3DMultisampleCoverageNV = caps.get(2154);
        this.glCreateSemaphoresNV = caps.get(2155);
        this.glSemaphoreParameterivNV = caps.get(2156);
        this.glGetSemaphoreParameterivNV = caps.get(2157);
        this.glBeginTransformFeedbackNV = caps.get(2158);
        this.glEndTransformFeedbackNV = caps.get(2159);
        this.glTransformFeedbackAttribsNV = caps.get(2160);
        this.glBindBufferRangeNV = caps.get(2161);
        this.glBindBufferOffsetNV = caps.get(2162);
        this.glBindBufferBaseNV = caps.get(2163);
        this.glTransformFeedbackVaryingsNV = caps.get(2164);
        this.glActiveVaryingNV = caps.get(2165);
        this.glGetVaryingLocationNV = caps.get(2166);
        this.glGetActiveVaryingNV = caps.get(2167);
        this.glGetTransformFeedbackVaryingNV = caps.get(2168);
        this.glTransformFeedbackStreamAttribsNV = caps.get(2169);
        this.glBindTransformFeedbackNV = caps.get(2170);
        this.glDeleteTransformFeedbacksNV = caps.get(2171);
        this.glGenTransformFeedbacksNV = caps.get(2172);
        this.glIsTransformFeedbackNV = caps.get(2173);
        this.glPauseTransformFeedbackNV = caps.get(2174);
        this.glResumeTransformFeedbackNV = caps.get(2175);
        this.glDrawTransformFeedbackNV = caps.get(2176);
        this.glVertexArrayRangeNV = caps.get(2177);
        this.glFlushVertexArrayRangeNV = caps.get(2178);
        this.glVertexAttribL1i64NV = caps.get(2179);
        this.glVertexAttribL2i64NV = caps.get(2180);
        this.glVertexAttribL3i64NV = caps.get(2181);
        this.glVertexAttribL4i64NV = caps.get(2182);
        this.glVertexAttribL1i64vNV = caps.get(2183);
        this.glVertexAttribL2i64vNV = caps.get(2184);
        this.glVertexAttribL3i64vNV = caps.get(2185);
        this.glVertexAttribL4i64vNV = caps.get(2186);
        this.glVertexAttribL1ui64NV = caps.get(2187);
        this.glVertexAttribL2ui64NV = caps.get(2188);
        this.glVertexAttribL3ui64NV = caps.get(2189);
        this.glVertexAttribL4ui64NV = caps.get(2190);
        this.glVertexAttribL1ui64vNV = caps.get(2191);
        this.glVertexAttribL2ui64vNV = caps.get(2192);
        this.glVertexAttribL3ui64vNV = caps.get(2193);
        this.glVertexAttribL4ui64vNV = caps.get(2194);
        this.glGetVertexAttribLi64vNV = caps.get(2195);
        this.glGetVertexAttribLui64vNV = caps.get(2196);
        this.glVertexAttribLFormatNV = caps.get(2197);
        this.glBufferAddressRangeNV = caps.get(2198);
        this.glVertexFormatNV = caps.get(2199);
        this.glNormalFormatNV = caps.get(2200);
        this.glColorFormatNV = caps.get(2201);
        this.glIndexFormatNV = caps.get(2202);
        this.glTexCoordFormatNV = caps.get(2203);
        this.glEdgeFlagFormatNV = caps.get(2204);
        this.glSecondaryColorFormatNV = caps.get(2205);
        this.glFogCoordFormatNV = caps.get(2206);
        this.glVertexAttribFormatNV = caps.get(2207);
        this.glVertexAttribIFormatNV = caps.get(2208);
        this.glGetIntegerui64i_vNV = caps.get(2209);
        this.glViewportSwizzleNV = caps.get(2210);
        this.glBeginConditionalRenderNVX = caps.get(2211);
        this.glEndConditionalRenderNVX = caps.get(2212);
        this.glAsyncCopyImageSubDataNVX = caps.get(2213);
        this.glAsyncCopyBufferSubDataNVX = caps.get(2214);
        this.glUploadGpuMaskNVX = caps.get(2215);
        this.glMulticastViewportArrayvNVX = caps.get(2216);
        this.glMulticastScissorArrayvNVX = caps.get(2217);
        this.glMulticastViewportPositionWScaleNVX = caps.get(2218);
        this.glCreateProgressFenceNVX = caps.get(2219);
        this.glSignalSemaphoreui64NVX = caps.get(2220);
        this.glWaitSemaphoreui64NVX = caps.get(2221);
        this.glClientWaitSemaphoreui64NVX = caps.get(2222);
        this.glFramebufferTextureMultiviewOVR = caps.get(2223);
        this.glNamedFramebufferTextureMultiviewOVR = caps.get(2224);
        this.addresses = ThreadLocalUtil.setupAddressBuffer(caps);
    }

    public PointerBuffer getAddressBuffer() {
        return this.addresses;
    }

    public static void initialize() {
    }

    private static boolean check_GL11(FunctionProvider provider, PointerBuffer caps, Set<String> ext, boolean fc) {
        if (!ext.contains("OpenGL11")) {
            return false;
        }
        int flag0 = !fc || ext.contains("GL_NV_vertex_buffer_unified_memory") ? 0 : Integer.MIN_VALUE;
        return (fc || Checks.checkFunctions(provider, caps, new int[]{2, 3, 4, 5, 6, 8, 10, 11, 13, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 52, 53, 54, 56, 64, 65, 66, 67, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 85, 86, 87, 88, 90, 93, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 110, 112, 113, 114, 115, 116, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 138, 140, 141, 142, 143, 144, 145, 146, 147, 148, 150, 151, 152, 153, 154, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 192, 193, 194, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 248, 249, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334}, "glAccum", "glAlphaFunc", "glAreTexturesResident", "glArrayElement", "glBegin", "glBitmap", "glCallList", "glCallLists", "glClearAccum", "glClearIndex", "glClipPlane", "glColor3b", "glColor3s", "glColor3i", "glColor3f", "glColor3d", "glColor3ub", "glColor3us", "glColor3ui", "glColor3bv", "glColor3sv", "glColor3iv", "glColor3fv", "glColor3dv", "glColor3ubv", "glColor3usv", "glColor3uiv", "glColor4b", "glColor4s", "glColor4i", "glColor4f", "glColor4d", "glColor4ub", "glColor4us", "glColor4ui", "glColor4bv", "glColor4sv", "glColor4iv", "glColor4fv", "glColor4dv", "glColor4ubv", "glColor4usv", "glColor4uiv", "glColorMaterial", "glColorPointer", "glCopyPixels", "glDeleteLists", "glDrawPixels", "glEdgeFlag", "glEdgeFlagv", "glEdgeFlagPointer", "glEnd", "glEvalCoord1f", "glEvalCoord1fv", "glEvalCoord1d", "glEvalCoord1dv", "glEvalCoord2f", "glEvalCoord2fv", "glEvalCoord2d", "glEvalCoord2dv", "glEvalMesh1", "glEvalMesh2", "glEvalPoint1", "glEvalPoint2", "glFeedbackBuffer", "glFogi", "glFogiv", "glFogf", "glFogfv", "glGenLists", "glGetClipPlane", "glGetLightiv", "glGetLightfv", "glGetMapiv", "glGetMapfv", "glGetMapdv", "glGetMaterialiv", "glGetMaterialfv", "glGetPixelMapfv", "glGetPixelMapusv", "glGetPixelMapuiv", "glGetPolygonStipple", "glGetTexEnviv", "glGetTexEnvfv", "glGetTexGeniv", "glGetTexGenfv", "glGetTexGendv", "glIndexi", "glIndexub", "glIndexs", "glIndexf", "glIndexd", "glIndexiv", "glIndexubv", "glIndexsv", "glIndexfv", "glIndexdv", "glIndexMask", "glIndexPointer", "glInitNames", "glInterleavedArrays", "glIsList", "glLightModeli", "glLightModelf", "glLightModeliv", "glLightModelfv", "glLighti", "glLightf", "glLightiv", "glLightfv", "glLineStipple", "glListBase", "glLoadMatrixf", "glLoadMatrixd", "glLoadIdentity", "glLoadName", "glMap1f", "glMap1d", "glMap2f", "glMap2d", "glMapGrid1f", "glMapGrid1d", "glMapGrid2f", "glMapGrid2d", "glMateriali", "glMaterialf", "glMaterialiv", "glMaterialfv", "glMatrixMode", "glMultMatrixf", "glMultMatrixd", "glFrustum", "glNewList", "glEndList", "glNormal3f", "glNormal3b", "glNormal3s", "glNormal3i", "glNormal3d", "glNormal3fv", "glNormal3bv", "glNormal3sv", "glNormal3iv", "glNormal3dv", "glNormalPointer", "glOrtho", "glPassThrough", "glPixelMapfv", "glPixelMapusv", "glPixelMapuiv", "glPixelTransferi", "glPixelTransferf", "glPixelZoom", "glPolygonStipple", "glPushAttrib", "glPushClientAttrib", "glPopAttrib", "glPopClientAttrib", "glPopMatrix", "glPopName", "glPrioritizeTextures", "glPushMatrix", "glPushName", "glRasterPos2i", "glRasterPos2s", "glRasterPos2f", "glRasterPos2d", "glRasterPos2iv", "glRasterPos2sv", "glRasterPos2fv", "glRasterPos2dv", "glRasterPos3i", "glRasterPos3s", "glRasterPos3f", "glRasterPos3d", "glRasterPos3iv", "glRasterPos3sv", "glRasterPos3fv", "glRasterPos3dv", "glRasterPos4i", "glRasterPos4s", "glRasterPos4f", "glRasterPos4d", "glRasterPos4iv", "glRasterPos4sv", "glRasterPos4fv", "glRasterPos4dv", "glRecti", "glRects", "glRectf", "glRectd", "glRectiv", "glRectsv", "glRectfv", "glRectdv", "glRenderMode", "glRotatef", "glRotated", "glScalef", "glScaled", "glSelectBuffer", "glShadeModel", "glTexCoord1f", "glTexCoord1s", "glTexCoord1i", "glTexCoord1d", "glTexCoord1fv", "glTexCoord1sv", "glTexCoord1iv", "glTexCoord1dv", "glTexCoord2f", "glTexCoord2s", "glTexCoord2i", "glTexCoord2d", "glTexCoord2fv", "glTexCoord2sv", "glTexCoord2iv", "glTexCoord2dv", "glTexCoord3f", "glTexCoord3s", "glTexCoord3i", "glTexCoord3d", "glTexCoord3fv", "glTexCoord3sv", "glTexCoord3iv", "glTexCoord3dv", "glTexCoord4f", "glTexCoord4s", "glTexCoord4i", "glTexCoord4d", "glTexCoord4fv", "glTexCoord4sv", "glTexCoord4iv", "glTexCoord4dv", "glTexCoordPointer", "glTexEnvi", "glTexEnviv", "glTexEnvf", "glTexEnvfv", "glTexGeni", "glTexGeniv", "glTexGenf", "glTexGenfv", "glTexGend", "glTexGendv", "glTranslatef", "glTranslated", "glVertex2f", "glVertex2s", "glVertex2i", "glVertex2d", "glVertex2fv", "glVertex2sv", "glVertex2iv", "glVertex2dv", "glVertex3f", "glVertex3s", "glVertex3i", "glVertex3d", "glVertex3fv", "glVertex3sv", "glVertex3iv", "glVertex3dv", "glVertex4f", "glVertex4s", "glVertex4i", "glVertex4d", "glVertex4fv", "glVertex4sv", "glVertex4iv", "glVertex4dv", "glVertexPointer")) && Checks.checkFunctions(provider, caps, new int[]{0, 1, 7, 9, 12, 14, 15, 17, 51, 55, 57, 58, 59, flag0 + 60, 61, 62, 63, flag0 + 68, 83, 84, 89, 91, 92, 94, 95, 96, 97, 98, 109, 111, 117, 118, 119, 120, 121, 122, 137, 139, 149, 155, 190, 191, 195, 196, 197, 232, 233, 247, 250, 251, 252, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 335}, "glEnable", "glDisable", "glBindTexture", "glBlendFunc", "glClear", "glClearColor", "glClearDepth", "glClearStencil", "glColorMask", "glCullFace", "glDepthFunc", "glDepthMask", "glDepthRange", "glDisableClientState", "glDrawArrays", "glDrawBuffer", "glDrawElements", "glEnableClientState", "glFinish", "glFlush", "glFrontFace", "glGenTextures", "glDeleteTextures", "glGetBooleanv", "glGetFloatv", "glGetIntegerv", "glGetDoublev", "glGetError", "glGetPointerv", "glGetString", "glGetTexImage", "glGetTexLevelParameteriv", "glGetTexLevelParameterfv", "glGetTexParameteriv", "glGetTexParameterfv", "glHint", "glIsEnabled", "glIsTexture", "glLineWidth", "glLogicOp", "glPixelStorei", "glPixelStoref", "glPointSize", "glPolygonMode", "glPolygonOffset", "glReadBuffer", "glReadPixels", "glScissor", "glStencilFunc", "glStencilMask", "glStencilOp", "glTexImage1D", "glTexImage2D", "glCopyTexImage1D", "glCopyTexImage2D", "glCopyTexSubImage1D", "glCopyTexSubImage2D", "glTexParameteri", "glTexParameteriv", "glTexParameterf", "glTexParameterfv", "glTexSubImage1D", "glTexSubImage2D", "glViewport") || Checks.reportMissing("GL", "OpenGL11");
    }

    private static boolean check_GL12(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL12")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{336, 337, 338, 339}, "glTexImage3D", "glTexSubImage3D", "glCopyTexSubImage3D", "glDrawRangeElements") || Checks.reportMissing("GL", "OpenGL12");
    }

    private static boolean check_GL13(FunctionProvider provider, PointerBuffer caps, Set<String> ext, boolean fc) {
        if (!ext.contains("OpenGL13")) {
            return false;
        }
        return (fc || Checks.checkFunctions(provider, caps, new int[]{349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385}, "glClientActiveTexture", "glMultiTexCoord1f", "glMultiTexCoord1s", "glMultiTexCoord1i", "glMultiTexCoord1d", "glMultiTexCoord1fv", "glMultiTexCoord1sv", "glMultiTexCoord1iv", "glMultiTexCoord1dv", "glMultiTexCoord2f", "glMultiTexCoord2s", "glMultiTexCoord2i", "glMultiTexCoord2d", "glMultiTexCoord2fv", "glMultiTexCoord2sv", "glMultiTexCoord2iv", "glMultiTexCoord2dv", "glMultiTexCoord3f", "glMultiTexCoord3s", "glMultiTexCoord3i", "glMultiTexCoord3d", "glMultiTexCoord3fv", "glMultiTexCoord3sv", "glMultiTexCoord3iv", "glMultiTexCoord3dv", "glMultiTexCoord4f", "glMultiTexCoord4s", "glMultiTexCoord4i", "glMultiTexCoord4d", "glMultiTexCoord4fv", "glMultiTexCoord4sv", "glMultiTexCoord4iv", "glMultiTexCoord4dv", "glLoadTransposeMatrixf", "glLoadTransposeMatrixd", "glMultTransposeMatrixf", "glMultTransposeMatrixd")) && Checks.checkFunctions(provider, caps, new int[]{340, 341, 342, 343, 344, 345, 346, 347, 348}, "glCompressedTexImage3D", "glCompressedTexImage2D", "glCompressedTexImage1D", "glCompressedTexSubImage3D", "glCompressedTexSubImage2D", "glCompressedTexSubImage1D", "glGetCompressedTexImage", "glSampleCoverage", "glActiveTexture") || Checks.reportMissing("GL", "OpenGL13");
    }

    private static boolean check_GL14(FunctionProvider provider, PointerBuffer caps, Set<String> ext, boolean fc) {
        if (!ext.contains("OpenGL14")) {
            return false;
        }
        return (fc || Checks.checkFunctions(provider, caps, new int[]{388, 389, 390, 391, 392, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 417, 418, 419, 420, 421, 422, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432}, "glFogCoordf", "glFogCoordd", "glFogCoordfv", "glFogCoorddv", "glFogCoordPointer", "glSecondaryColor3b", "glSecondaryColor3s", "glSecondaryColor3i", "glSecondaryColor3f", "glSecondaryColor3d", "glSecondaryColor3ub", "glSecondaryColor3us", "glSecondaryColor3ui", "glSecondaryColor3bv", "glSecondaryColor3sv", "glSecondaryColor3iv", "glSecondaryColor3fv", "glSecondaryColor3dv", "glSecondaryColor3ubv", "glSecondaryColor3usv", "glSecondaryColor3uiv", "glSecondaryColorPointer", "glWindowPos2i", "glWindowPos2s", "glWindowPos2f", "glWindowPos2d", "glWindowPos2iv", "glWindowPos2sv", "glWindowPos2fv", "glWindowPos2dv", "glWindowPos3i", "glWindowPos3s", "glWindowPos3f", "glWindowPos3d", "glWindowPos3iv", "glWindowPos3sv", "glWindowPos3fv", "glWindowPos3dv")) && Checks.checkFunctions(provider, caps, new int[]{386, 387, 393, 394, 395, 396, 397, 398, 416}, "glBlendColor", "glBlendEquation", "glMultiDrawArrays", "glMultiDrawElements", "glPointParameterf", "glPointParameteri", "glPointParameterfv", "glPointParameteriv", "glBlendFuncSeparate") || Checks.reportMissing("GL", "OpenGL14");
    }

    private static boolean check_GL15(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL15")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 444, 445, 446, 447, 448, 449, 450, 451}, "glBindBuffer", "glDeleteBuffers", "glGenBuffers", "glIsBuffer", "glBufferData", "glBufferSubData", "glGetBufferSubData", "glMapBuffer", "glUnmapBuffer", "glGetBufferParameteriv", "glGetBufferPointerv", "glGenQueries", "glDeleteQueries", "glIsQuery", "glBeginQuery", "glEndQuery", "glGetQueryiv", "glGetQueryObjectiv", "glGetQueryObjectuiv") || Checks.reportMissing("GL", "OpenGL15");
    }

    private static boolean check_GL20(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL20")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 516, 517, 518, 519, 520, 521, 522, 523, 524, 525, 526, 527, 528, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 542, 543, 544}, "glCreateProgram", "glDeleteProgram", "glIsProgram", "glCreateShader", "glDeleteShader", "glIsShader", "glAttachShader", "glDetachShader", "glShaderSource", "glCompileShader", "glLinkProgram", "glUseProgram", "glValidateProgram", "glUniform1f", "glUniform2f", "glUniform3f", "glUniform4f", "glUniform1i", "glUniform2i", "glUniform3i", "glUniform4i", "glUniform1fv", "glUniform2fv", "glUniform3fv", "glUniform4fv", "glUniform1iv", "glUniform2iv", "glUniform3iv", "glUniform4iv", "glUniformMatrix2fv", "glUniformMatrix3fv", "glUniformMatrix4fv", "glGetShaderiv", "glGetProgramiv", "glGetShaderInfoLog", "glGetProgramInfoLog", "glGetAttachedShaders", "glGetUniformLocation", "glGetActiveUniform", "glGetUniformfv", "glGetUniformiv", "glGetShaderSource", "glVertexAttrib1f", "glVertexAttrib1s", "glVertexAttrib1d", "glVertexAttrib2f", "glVertexAttrib2s", "glVertexAttrib2d", "glVertexAttrib3f", "glVertexAttrib3s", "glVertexAttrib3d", "glVertexAttrib4f", "glVertexAttrib4s", "glVertexAttrib4d", "glVertexAttrib4Nub", "glVertexAttrib1fv", "glVertexAttrib1sv", "glVertexAttrib1dv", "glVertexAttrib2fv", "glVertexAttrib2sv", "glVertexAttrib2dv", "glVertexAttrib3fv", "glVertexAttrib3sv", "glVertexAttrib3dv", "glVertexAttrib4fv", "glVertexAttrib4sv", "glVertexAttrib4dv", "glVertexAttrib4iv", "glVertexAttrib4bv", "glVertexAttrib4ubv", "glVertexAttrib4usv", "glVertexAttrib4uiv", "glVertexAttrib4Nbv", "glVertexAttrib4Nsv", "glVertexAttrib4Niv", "glVertexAttrib4Nubv", "glVertexAttrib4Nusv", "glVertexAttrib4Nuiv", "glVertexAttribPointer", "glEnableVertexAttribArray", "glDisableVertexAttribArray", "glBindAttribLocation", "glGetActiveAttrib", "glGetAttribLocation", "glGetVertexAttribiv", "glGetVertexAttribfv", "glGetVertexAttribdv", "glGetVertexAttribPointerv", "glDrawBuffers", "glBlendEquationSeparate", "glStencilOpSeparate", "glStencilFuncSeparate", "glStencilMaskSeparate") || Checks.reportMissing("GL", "OpenGL20");
    }

    private static boolean check_GL21(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL21")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{545, 546, 547, 548, 549, 550}, "glUniformMatrix2x3fv", "glUniformMatrix3x2fv", "glUniformMatrix2x4fv", "glUniformMatrix4x2fv", "glUniformMatrix3x4fv", "glUniformMatrix4x3fv") || Checks.reportMissing("GL", "OpenGL21");
    }

    private static boolean check_GL30(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL30")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 577, 578, 579, 580, 581, 582, 583, 584, 585, 586, 587, 588, 589, 590, 591, 592, 593, 594, 595, 596, 597, 598, 599, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 610, 611, 612, 613, 614, 615, 616, 617, 618, 619, 620, 621, 622, 623, 624, 625, 626, 627, 628, 629, 630, 631, 632, 633, 634}, "glGetStringi", "glClearBufferiv", "glClearBufferuiv", "glClearBufferfv", "glClearBufferfi", "glVertexAttribI1i", "glVertexAttribI2i", "glVertexAttribI3i", "glVertexAttribI4i", "glVertexAttribI1ui", "glVertexAttribI2ui", "glVertexAttribI3ui", "glVertexAttribI4ui", "glVertexAttribI1iv", "glVertexAttribI2iv", "glVertexAttribI3iv", "glVertexAttribI4iv", "glVertexAttribI1uiv", "glVertexAttribI2uiv", "glVertexAttribI3uiv", "glVertexAttribI4uiv", "glVertexAttribI4bv", "glVertexAttribI4sv", "glVertexAttribI4ubv", "glVertexAttribI4usv", "glVertexAttribIPointer", "glGetVertexAttribIiv", "glGetVertexAttribIuiv", "glUniform1ui", "glUniform2ui", "glUniform3ui", "glUniform4ui", "glUniform1uiv", "glUniform2uiv", "glUniform3uiv", "glUniform4uiv", "glGetUniformuiv", "glBindFragDataLocation", "glGetFragDataLocation", "glBeginConditionalRender", "glEndConditionalRender", "glMapBufferRange", "glFlushMappedBufferRange", "glClampColor", "glIsRenderbuffer", "glBindRenderbuffer", "glDeleteRenderbuffers", "glGenRenderbuffers", "glRenderbufferStorage", "glRenderbufferStorageMultisample", "glGetRenderbufferParameteriv", "glIsFramebuffer", "glBindFramebuffer", "glDeleteFramebuffers", "glGenFramebuffers", "glCheckFramebufferStatus", "glFramebufferTexture1D", "glFramebufferTexture2D", "glFramebufferTexture3D", "glFramebufferTextureLayer", "glFramebufferRenderbuffer", "glGetFramebufferAttachmentParameteriv", "glBlitFramebuffer", "glGenerateMipmap", "glTexParameterIiv", "glTexParameterIuiv", "glGetTexParameterIiv", "glGetTexParameterIuiv", "glColorMaski", "glGetBooleani_v", "glGetIntegeri_v", "glEnablei", "glDisablei", "glIsEnabledi", "glBindBufferRange", "glBindBufferBase", "glBeginTransformFeedback", "glEndTransformFeedback", "glTransformFeedbackVaryings", "glGetTransformFeedbackVarying", "glBindVertexArray", "glDeleteVertexArrays", "glGenVertexArrays", "glIsVertexArray") || Checks.reportMissing("GL", "OpenGL30");
    }

    private static boolean check_GL31(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL31")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{635, 636, 637, 638, 639, 640, 641, 642, 643, 644, 645, 646}, "glDrawArraysInstanced", "glDrawElementsInstanced", "glCopyBufferSubData", "glPrimitiveRestartIndex", "glTexBuffer", "glGetUniformIndices", "glGetActiveUniformsiv", "glGetActiveUniformName", "glGetUniformBlockIndex", "glGetActiveUniformBlockiv", "glGetActiveUniformBlockName", "glUniformBlockBinding") || Checks.reportMissing("GL", "OpenGL31");
    }

    private static boolean check_GL32(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL32")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662, 663, 664, 665}, "glGetBufferParameteri64v", "glDrawElementsBaseVertex", "glDrawRangeElementsBaseVertex", "glDrawElementsInstancedBaseVertex", "glMultiDrawElementsBaseVertex", "glProvokingVertex", "glTexImage2DMultisample", "glTexImage3DMultisample", "glGetMultisamplefv", "glSampleMaski", "glFramebufferTexture", "glFenceSync", "glIsSync", "glDeleteSync", "glClientWaitSync", "glWaitSync", "glGetInteger64v", "glGetInteger64i_v", "glGetSynciv") || Checks.reportMissing("GL", "OpenGL32");
    }

    private static boolean check_GL33(FunctionProvider provider, PointerBuffer caps, Set<String> ext, boolean fc) {
        if (!ext.contains("OpenGL33")) {
            return false;
        }
        return (fc || Checks.checkFunctions(provider, caps, new int[]{686, 687, 688, 689, 690, 691, 692, 693, 694, 695, 696, 697, 698, 699, 700, 701, 702, 703, 704, 705, 706, 707, 708, 709, 710, 711, 712, 713, 714, 715}, "glVertexP2ui", "glVertexP3ui", "glVertexP4ui", "glVertexP2uiv", "glVertexP3uiv", "glVertexP4uiv", "glTexCoordP1ui", "glTexCoordP2ui", "glTexCoordP3ui", "glTexCoordP4ui", "glTexCoordP1uiv", "glTexCoordP2uiv", "glTexCoordP3uiv", "glTexCoordP4uiv", "glMultiTexCoordP1ui", "glMultiTexCoordP2ui", "glMultiTexCoordP3ui", "glMultiTexCoordP4ui", "glMultiTexCoordP1uiv", "glMultiTexCoordP2uiv", "glMultiTexCoordP3uiv", "glMultiTexCoordP4uiv", "glNormalP3ui", "glNormalP3uiv", "glColorP3ui", "glColorP4ui", "glColorP3uiv", "glColorP4uiv", "glSecondaryColorP3ui", "glSecondaryColorP3uiv")) && Checks.checkFunctions(provider, caps, new int[]{666, 667, 668, 669, 670, 671, 672, 673, 674, 675, 676, 677, 678, 679, 680, 681, 682, 683, 684, 685, 716, 717, 718, 719, 720, 721, 722, 723}, "glBindFragDataLocationIndexed", "glGetFragDataIndex", "glGenSamplers", "glDeleteSamplers", "glIsSampler", "glBindSampler", "glSamplerParameteri", "glSamplerParameterf", "glSamplerParameteriv", "glSamplerParameterfv", "glSamplerParameterIiv", "glSamplerParameterIuiv", "glGetSamplerParameteriv", "glGetSamplerParameterfv", "glGetSamplerParameterIiv", "glGetSamplerParameterIuiv", "glQueryCounter", "glGetQueryObjecti64v", "glGetQueryObjectui64v", "glVertexAttribDivisor", "glVertexAttribP1ui", "glVertexAttribP2ui", "glVertexAttribP3ui", "glVertexAttribP4ui", "glVertexAttribP1uiv", "glVertexAttribP2uiv", "glVertexAttribP3uiv", "glVertexAttribP4uiv") || Checks.reportMissing("GL", "OpenGL33");
    }

    private static boolean check_GL40(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL40")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{724, 725, 726, 727, 728, 729, 730, 731, 732, 733, 734, 735, 736, 737, 738, 739, 740, 741, 742, 743, 744, 745, 746, 747, 748, 749, 750, 751, 752, 753, 754, 755, 756, 757, 758, 759, 760, 761, 762, 763, 764, 765, 766, 767, 768, 769}, "glBlendEquationi", "glBlendEquationSeparatei", "glBlendFunci", "glBlendFuncSeparatei", "glDrawArraysIndirect", "glDrawElementsIndirect", "glUniform1d", "glUniform2d", "glUniform3d", "glUniform4d", "glUniform1dv", "glUniform2dv", "glUniform3dv", "glUniform4dv", "glUniformMatrix2dv", "glUniformMatrix3dv", "glUniformMatrix4dv", "glUniformMatrix2x3dv", "glUniformMatrix2x4dv", "glUniformMatrix3x2dv", "glUniformMatrix3x4dv", "glUniformMatrix4x2dv", "glUniformMatrix4x3dv", "glGetUniformdv", "glMinSampleShading", "glGetSubroutineUniformLocation", "glGetSubroutineIndex", "glGetActiveSubroutineUniformiv", "glGetActiveSubroutineUniformName", "glGetActiveSubroutineName", "glUniformSubroutinesuiv", "glGetUniformSubroutineuiv", "glGetProgramStageiv", "glPatchParameteri", "glPatchParameterfv", "glBindTransformFeedback", "glDeleteTransformFeedbacks", "glGenTransformFeedbacks", "glIsTransformFeedback", "glPauseTransformFeedback", "glResumeTransformFeedback", "glDrawTransformFeedback", "glDrawTransformFeedbackStream", "glBeginQueryIndexed", "glEndQueryIndexed", "glGetQueryIndexediv") || Checks.reportMissing("GL", "OpenGL40");
    }

    private static boolean check_GL41(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL41")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{770, 771, 772, 773, 774, 775, 776, 777, 778, 779, 780, 781, 782, 783, 784, 785, 786, 787, 788, 789, 790, 791, 792, 793, 794, 795, 796, 797, 798, 799, 800, 801, 802, 803, 804, 805, 806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 816, 817, 818, 819, 820, 821, 822, 823, 824, 825, 826, 827, 828, 829, 830, 831, 832, 833, 834, 835, 836, 837, 838, 839, 840, 841, 842, 843, 844, 845, 846, 847, 848, 849, 850, 851, 852, 853, 854, 855, 856, 857}, "glReleaseShaderCompiler", "glShaderBinary", "glGetShaderPrecisionFormat", "glDepthRangef", "glClearDepthf", "glGetProgramBinary", "glProgramBinary", "glProgramParameteri", "glUseProgramStages", "glActiveShaderProgram", "glCreateShaderProgramv", "glBindProgramPipeline", "glDeleteProgramPipelines", "glGenProgramPipelines", "glIsProgramPipeline", "glGetProgramPipelineiv", "glProgramUniform1i", "glProgramUniform2i", "glProgramUniform3i", "glProgramUniform4i", "glProgramUniform1ui", "glProgramUniform2ui", "glProgramUniform3ui", "glProgramUniform4ui", "glProgramUniform1f", "glProgramUniform2f", "glProgramUniform3f", "glProgramUniform4f", "glProgramUniform1d", "glProgramUniform2d", "glProgramUniform3d", "glProgramUniform4d", "glProgramUniform1iv", "glProgramUniform2iv", "glProgramUniform3iv", "glProgramUniform4iv", "glProgramUniform1uiv", "glProgramUniform2uiv", "glProgramUniform3uiv", "glProgramUniform4uiv", "glProgramUniform1fv", "glProgramUniform2fv", "glProgramUniform3fv", "glProgramUniform4fv", "glProgramUniform1dv", "glProgramUniform2dv", "glProgramUniform3dv", "glProgramUniform4dv", "glProgramUniformMatrix2fv", "glProgramUniformMatrix3fv", "glProgramUniformMatrix4fv", "glProgramUniformMatrix2dv", "glProgramUniformMatrix3dv", "glProgramUniformMatrix4dv", "glProgramUniformMatrix2x3fv", "glProgramUniformMatrix3x2fv", "glProgramUniformMatrix2x4fv", "glProgramUniformMatrix4x2fv", "glProgramUniformMatrix3x4fv", "glProgramUniformMatrix4x3fv", "glProgramUniformMatrix2x3dv", "glProgramUniformMatrix3x2dv", "glProgramUniformMatrix2x4dv", "glProgramUniformMatrix4x2dv", "glProgramUniformMatrix3x4dv", "glProgramUniformMatrix4x3dv", "glValidateProgramPipeline", "glGetProgramPipelineInfoLog", "glVertexAttribL1d", "glVertexAttribL2d", "glVertexAttribL3d", "glVertexAttribL4d", "glVertexAttribL1dv", "glVertexAttribL2dv", "glVertexAttribL3dv", "glVertexAttribL4dv", "glVertexAttribLPointer", "glGetVertexAttribLdv", "glViewportArrayv", "glViewportIndexedf", "glViewportIndexedfv", "glScissorArrayv", "glScissorIndexed", "glScissorIndexedv", "glDepthRangeArrayv", "glDepthRangeIndexed", "glGetFloati_v", "glGetDoublei_v") || Checks.reportMissing("GL", "OpenGL41");
    }

    private static boolean check_GL42(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL42")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{858, 859, 860, 861, 862, 863, 864, 865, 866, 867, 868, 869}, "glGetActiveAtomicCounterBufferiv", "glTexStorage1D", "glTexStorage2D", "glTexStorage3D", "glDrawTransformFeedbackInstanced", "glDrawTransformFeedbackStreamInstanced", "glDrawArraysInstancedBaseInstance", "glDrawElementsInstancedBaseInstance", "glDrawElementsInstancedBaseVertexBaseInstance", "glBindImageTexture", "glMemoryBarrier", "glGetInternalformativ") || Checks.reportMissing("GL", "OpenGL42");
    }

    private static boolean check_GL43(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL43")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{870, 871, 872, 873, 874, 875, 876, 877, 878, 879, 880, 881, 882, 883, 884, 885, 886, 887, 888, 889, 890, 891, 892, 893, 894, 895, 896, 897, 898, 899, 900, 901, 902, 903, 904, 905, 906, 907, 908, 909, 910, 911, 912}, "glClearBufferData", "glClearBufferSubData", "glDispatchCompute", "glDispatchComputeIndirect", "glCopyImageSubData", "glDebugMessageControl", "glDebugMessageInsert", "glDebugMessageCallback", "glGetDebugMessageLog", "glPushDebugGroup", "glPopDebugGroup", "glObjectLabel", "glGetObjectLabel", "glObjectPtrLabel", "glGetObjectPtrLabel", "glFramebufferParameteri", "glGetFramebufferParameteriv", "glGetInternalformati64v", "glInvalidateTexSubImage", "glInvalidateTexImage", "glInvalidateBufferSubData", "glInvalidateBufferData", "glInvalidateFramebuffer", "glInvalidateSubFramebuffer", "glMultiDrawArraysIndirect", "glMultiDrawElementsIndirect", "glGetProgramInterfaceiv", "glGetProgramResourceIndex", "glGetProgramResourceName", "glGetProgramResourceiv", "glGetProgramResourceLocation", "glGetProgramResourceLocationIndex", "glShaderStorageBlockBinding", "glTexBufferRange", "glTexStorage2DMultisample", "glTexStorage3DMultisample", "glTextureView", "glBindVertexBuffer", "glVertexAttribFormat", "glVertexAttribIFormat", "glVertexAttribLFormat", "glVertexAttribBinding", "glVertexBindingDivisor") || Checks.reportMissing("GL", "OpenGL43");
    }

    private static boolean check_GL44(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL44")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{913, 914, 915, 916, 917, 918, 919, 920, 921}, "glBufferStorage", "glClearTexSubImage", "glClearTexImage", "glBindBuffersBase", "glBindBuffersRange", "glBindTextures", "glBindSamplers", "glBindImageTextures", "glBindVertexBuffers") || Checks.reportMissing("GL", "OpenGL44");
    }

    private static boolean check_GL45(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL45")) {
            return false;
        }
        int flag0 = provider.getFunctionAddress("glGetMapdv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag1 = provider.getFunctionAddress("glGetMapfv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag2 = provider.getFunctionAddress("glGetMapiv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag3 = provider.getFunctionAddress("glGetPixelMapfv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag4 = provider.getFunctionAddress("glGetPixelMapuiv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag5 = provider.getFunctionAddress("glGetPixelMapusv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag6 = provider.getFunctionAddress("glGetPolygonStipple") != 0L ? 0 : Integer.MIN_VALUE;
        int flag7 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetColorTable") != 0L ? 0 : Integer.MIN_VALUE;
        int flag8 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetConvolutionFilter") != 0L ? 0 : Integer.MIN_VALUE;
        int flag9 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetSeparableFilter") != 0L ? 0 : Integer.MIN_VALUE;
        int flag10 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetHistogram") != 0L ? 0 : Integer.MIN_VALUE;
        int flag11 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetMinmax") != 0L ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{922, 923, 924, 925, 926, 927, 928, 929, 930, 931, 932, 933, 934, 935, 936, 937, 938, 939, 940, 941, 942, 943, 944, 945, 946, 947, 948, 949, 950, 951, 952, 953, 954, 955, 956, 957, 958, 959, 960, 961, 962, 963, 964, 965, 966, 967, 968, 969, 970, 971, 972, 973, 974, 975, 976, 977, 978, 979, 980, 981, 982, 983, 984, 985, 986, 987, 988, 989, 990, 991, 992, 993, 994, 995, 996, 997, 998, 999, 1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1033, 1040, 1042, 1043}, "glClipControl", "glCreateTransformFeedbacks", "glTransformFeedbackBufferBase", "glTransformFeedbackBufferRange", "glGetTransformFeedbackiv", "glGetTransformFeedbacki_v", "glGetTransformFeedbacki64_v", "glCreateBuffers", "glNamedBufferStorage", "glNamedBufferData", "glNamedBufferSubData", "glCopyNamedBufferSubData", "glClearNamedBufferData", "glClearNamedBufferSubData", "glMapNamedBuffer", "glMapNamedBufferRange", "glUnmapNamedBuffer", "glFlushMappedNamedBufferRange", "glGetNamedBufferParameteriv", "glGetNamedBufferParameteri64v", "glGetNamedBufferPointerv", "glGetNamedBufferSubData", "glCreateFramebuffers", "glNamedFramebufferRenderbuffer", "glNamedFramebufferParameteri", "glNamedFramebufferTexture", "glNamedFramebufferTextureLayer", "glNamedFramebufferDrawBuffer", "glNamedFramebufferDrawBuffers", "glNamedFramebufferReadBuffer", "glInvalidateNamedFramebufferData", "glInvalidateNamedFramebufferSubData", "glClearNamedFramebufferiv", "glClearNamedFramebufferuiv", "glClearNamedFramebufferfv", "glClearNamedFramebufferfi", "glBlitNamedFramebuffer", "glCheckNamedFramebufferStatus", "glGetNamedFramebufferParameteriv", "glGetNamedFramebufferAttachmentParameteriv", "glCreateRenderbuffers", "glNamedRenderbufferStorage", "glNamedRenderbufferStorageMultisample", "glGetNamedRenderbufferParameteriv", "glCreateTextures", "glTextureBuffer", "glTextureBufferRange", "glTextureStorage1D", "glTextureStorage2D", "glTextureStorage3D", "glTextureStorage2DMultisample", "glTextureStorage3DMultisample", "glTextureSubImage1D", "glTextureSubImage2D", "glTextureSubImage3D", "glCompressedTextureSubImage1D", "glCompressedTextureSubImage2D", "glCompressedTextureSubImage3D", "glCopyTextureSubImage1D", "glCopyTextureSubImage2D", "glCopyTextureSubImage3D", "glTextureParameterf", "glTextureParameterfv", "glTextureParameteri", "glTextureParameterIiv", "glTextureParameterIuiv", "glTextureParameteriv", "glGenerateTextureMipmap", "glBindTextureUnit", "glGetTextureImage", "glGetCompressedTextureImage", "glGetTextureLevelParameterfv", "glGetTextureLevelParameteriv", "glGetTextureParameterfv", "glGetTextureParameterIiv", "glGetTextureParameterIuiv", "glGetTextureParameteriv", "glCreateVertexArrays", "glDisableVertexArrayAttrib", "glEnableVertexArrayAttrib", "glVertexArrayElementBuffer", "glVertexArrayVertexBuffer", "glVertexArrayVertexBuffers", "glVertexArrayAttribFormat", "glVertexArrayAttribIFormat", "glVertexArrayAttribLFormat", "glVertexArrayAttribBinding", "glVertexArrayBindingDivisor", "glGetVertexArrayiv", "glGetVertexArrayIndexediv", "glGetVertexArrayIndexed64iv", "glCreateSamplers", "glCreateProgramPipelines", "glCreateQueries", "glGetQueryBufferObjectiv", "glGetQueryBufferObjectuiv", "glGetQueryBufferObjecti64v", "glGetQueryBufferObjectui64v", "glMemoryBarrierByRegion", "glGetTextureSubImage", "glGetCompressedTextureSubImage", "glTextureBarrier", "glGetGraphicsResetStatus", "glReadnPixels", "glGetnUniformfv", "glGetnUniformiv", "glGetnUniformuiv") || Checks.reportMissing("GL", "OpenGL45");
    }

    private static boolean check_GL46(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenGL46")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1044, 1045, 1046, 1047}, "glMultiDrawArraysIndirectCount", "glMultiDrawElementsIndirectCount", "glPolygonOffsetClamp", "glSpecializeShader") || Checks.reportMissing("GL", "OpenGL46");
    }

    private static boolean check_AMD_debug_output(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_debug_output")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1048, 1049, 1050, 1051}, "glDebugMessageEnableAMD", "glDebugMessageInsertAMD", "glDebugMessageCallbackAMD", "glGetDebugMessageLogAMD") || Checks.reportMissing("GL", "GL_AMD_debug_output");
    }

    private static boolean check_AMD_draw_buffers_blend(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_draw_buffers_blend")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1052, 1053, 1054, 1055}, "glBlendFuncIndexedAMD", "glBlendFuncSeparateIndexedAMD", "glBlendEquationIndexedAMD", "glBlendEquationSeparateIndexedAMD") || Checks.reportMissing("GL", "GL_AMD_draw_buffers_blend");
    }

    private static boolean check_AMD_framebuffer_multisample_advanced(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_framebuffer_multisample_advanced")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1056, 1057}, "glRenderbufferStorageMultisampleAdvancedAMD", "glNamedRenderbufferStorageMultisampleAdvancedAMD") || Checks.reportMissing("GL", "GL_AMD_framebuffer_multisample_advanced");
    }

    private static boolean check_AMD_gpu_shader_int64(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_gpu_shader_int64")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1058, 1059, 1060, 1061, 1062, 1063, 1064, 1065, 1066, 1067, 1068, 1069, 1070, 1071, 1072, 1073, 1074, 1075, flag0 + 1076, flag0 + 1077, flag0 + 1078, flag0 + 1079, flag0 + 1080, flag0 + 1081, flag0 + 1082, flag0 + 1083, flag0 + 1084, flag0 + 1085, flag0 + 1086, flag0 + 1087, flag0 + 1088, flag0 + 1089, flag0 + 1090, flag0 + 1091}, "glUniform1i64NV", "glUniform2i64NV", "glUniform3i64NV", "glUniform4i64NV", "glUniform1i64vNV", "glUniform2i64vNV", "glUniform3i64vNV", "glUniform4i64vNV", "glUniform1ui64NV", "glUniform2ui64NV", "glUniform3ui64NV", "glUniform4ui64NV", "glUniform1ui64vNV", "glUniform2ui64vNV", "glUniform3ui64vNV", "glUniform4ui64vNV", "glGetUniformi64vNV", "glGetUniformui64vNV", "glProgramUniform1i64NV", "glProgramUniform2i64NV", "glProgramUniform3i64NV", "glProgramUniform4i64NV", "glProgramUniform1i64vNV", "glProgramUniform2i64vNV", "glProgramUniform3i64vNV", "glProgramUniform4i64vNV", "glProgramUniform1ui64NV", "glProgramUniform2ui64NV", "glProgramUniform3ui64NV", "glProgramUniform4ui64NV", "glProgramUniform1ui64vNV", "glProgramUniform2ui64vNV", "glProgramUniform3ui64vNV", "glProgramUniform4ui64vNV") || Checks.reportMissing("GL", "GL_AMD_gpu_shader_int64");
    }

    private static boolean check_AMD_interleaved_elements(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_interleaved_elements")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1092}, "glVertexAttribParameteriAMD") || Checks.reportMissing("GL", "GL_AMD_interleaved_elements");
    }

    private static boolean check_AMD_occlusion_query_event(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_occlusion_query_event")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1093}, "glQueryObjectParameteruiAMD") || Checks.reportMissing("GL", "GL_AMD_occlusion_query_event");
    }

    private static boolean check_AMD_performance_monitor(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_performance_monitor")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1094, 1095, 1096, 1097, 1098, 1099, 1100, 1101, 1102, 1103, 1104}, "glGetPerfMonitorGroupsAMD", "glGetPerfMonitorCountersAMD", "glGetPerfMonitorGroupStringAMD", "glGetPerfMonitorCounterStringAMD", "glGetPerfMonitorCounterInfoAMD", "glGenPerfMonitorsAMD", "glDeletePerfMonitorsAMD", "glSelectPerfMonitorCountersAMD", "glBeginPerfMonitorAMD", "glEndPerfMonitorAMD", "glGetPerfMonitorCounterDataAMD") || Checks.reportMissing("GL", "GL_AMD_performance_monitor");
    }

    private static boolean check_AMD_sample_positions(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_sample_positions")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1105}, "glSetMultisamplefvAMD") || Checks.reportMissing("GL", "GL_AMD_sample_positions");
    }

    private static boolean check_AMD_sparse_texture(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_sparse_texture")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1106, 1107}, "glTexStorageSparseAMD", "glTextureStorageSparseAMD") || Checks.reportMissing("GL", "GL_AMD_sparse_texture");
    }

    private static boolean check_AMD_stencil_operation_extended(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_stencil_operation_extended")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1108}, "glStencilOpValueAMD") || Checks.reportMissing("GL", "GL_AMD_stencil_operation_extended");
    }

    private static boolean check_AMD_vertex_shader_tessellator(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_AMD_vertex_shader_tessellator")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1109, 1110}, "glTessellationFactorAMD", "glTessellationModeAMD") || Checks.reportMissing("GL", "GL_AMD_vertex_shader_tessellator");
    }

    private static boolean check_ARB_base_instance(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_base_instance")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{864, 865, 866}, "glDrawArraysInstancedBaseInstance", "glDrawElementsInstancedBaseInstance", "glDrawElementsInstancedBaseVertexBaseInstance") || Checks.reportMissing("GL", "GL_ARB_base_instance");
    }

    private static boolean check_ARB_bindless_texture(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_bindless_texture")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1111, 1112, 1113, 1114, 1115, 1116, 1117, 1118, 1119, 1120, 1121, 1122, 1123, 1124, 1125, 1126}, "glGetTextureHandleARB", "glGetTextureSamplerHandleARB", "glMakeTextureHandleResidentARB", "glMakeTextureHandleNonResidentARB", "glGetImageHandleARB", "glMakeImageHandleResidentARB", "glMakeImageHandleNonResidentARB", "glUniformHandleui64ARB", "glUniformHandleui64vARB", "glProgramUniformHandleui64ARB", "glProgramUniformHandleui64vARB", "glIsTextureHandleResidentARB", "glIsImageHandleResidentARB", "glVertexAttribL1ui64ARB", "glVertexAttribL1ui64vARB", "glGetVertexAttribLui64vARB") || Checks.reportMissing("GL", "GL_ARB_bindless_texture");
    }

    private static boolean check_ARB_blend_func_extended(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_blend_func_extended")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{666, 667}, "glBindFragDataLocationIndexed", "glGetFragDataIndex") || Checks.reportMissing("GL", "GL_ARB_blend_func_extended");
    }

    private static boolean check_ARB_buffer_storage(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_buffer_storage")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{913, flag0 + 1127}, "glBufferStorage", "glNamedBufferStorageEXT") || Checks.reportMissing("GL", "GL_ARB_buffer_storage");
    }

    private static boolean check_ARB_cl_event(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_cl_event")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1128}, "glCreateSyncFromCLeventARB") || Checks.reportMissing("GL", "GL_ARB_cl_event");
    }

    private static boolean check_ARB_clear_buffer_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_clear_buffer_object")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{870, 871, flag0 + 1129, flag0 + 1130}, "glClearBufferData", "glClearBufferSubData", "glClearNamedBufferDataEXT", "glClearNamedBufferSubDataEXT") || Checks.reportMissing("GL", "GL_ARB_clear_buffer_object");
    }

    private static boolean check_ARB_clear_texture(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_clear_texture")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{914, 915}, "glClearTexSubImage", "glClearTexImage") || Checks.reportMissing("GL", "GL_ARB_clear_texture");
    }

    private static boolean check_ARB_clip_control(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_clip_control")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{922}, "glClipControl") || Checks.reportMissing("GL", "GL_ARB_clip_control");
    }

    private static boolean check_ARB_color_buffer_float(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_color_buffer_float")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1131}, "glClampColorARB") || Checks.reportMissing("GL", "GL_ARB_color_buffer_float");
    }

    private static boolean check_ARB_compute_shader(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_compute_shader")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{872, 873}, "glDispatchCompute", "glDispatchComputeIndirect") || Checks.reportMissing("GL", "GL_ARB_compute_shader");
    }

    private static boolean check_ARB_compute_variable_group_size(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_compute_variable_group_size")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1132}, "glDispatchComputeGroupSizeARB") || Checks.reportMissing("GL", "GL_ARB_compute_variable_group_size");
    }

    private static boolean check_ARB_copy_buffer(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_copy_buffer")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{637}, "glCopyBufferSubData") || Checks.reportMissing("GL", "GL_ARB_copy_buffer");
    }

    private static boolean check_ARB_copy_image(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_copy_image")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{874}, "glCopyImageSubData") || Checks.reportMissing("GL", "GL_ARB_copy_image");
    }

    private static boolean check_ARB_debug_output(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_debug_output")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1133, 1134, 1135, 1136}, "glDebugMessageControlARB", "glDebugMessageInsertARB", "glDebugMessageCallbackARB", "glGetDebugMessageLogARB") || Checks.reportMissing("GL", "GL_ARB_debug_output");
    }

    private static boolean check_ARB_direct_state_access(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_direct_state_access")) {
            return false;
        }
        int flag0 = GLCapabilities.ARB_transform_feedback2(ext) ? 0 : Integer.MIN_VALUE;
        int flag1 = GLCapabilities.ARB_uniform_buffer_object(ext) ? 0 : Integer.MIN_VALUE;
        int flag6 = GLCapabilities.ARB_buffer_storage(ext) ? 0 : Integer.MIN_VALUE;
        int flag7 = GLCapabilities.ARB_copy_buffer(ext) ? 0 : Integer.MIN_VALUE;
        int flag8 = GLCapabilities.ARB_clear_texture(ext) ? 0 : Integer.MIN_VALUE;
        int flag10 = GLCapabilities.ARB_map_buffer_range(ext) ? 0 : Integer.MIN_VALUE;
        int flag12 = GLCapabilities.ARB_framebuffer_object(ext) ? 0 : Integer.MIN_VALUE;
        int flag14 = GLCapabilities.ARB_framebuffer_no_attachments(ext) ? 0 : Integer.MIN_VALUE;
        int flag20 = GLCapabilities.ARB_invalidate_subdata(ext) ? 0 : Integer.MIN_VALUE;
        int flag34 = GLCapabilities.ARB_texture_buffer_object(ext) ? 0 : Integer.MIN_VALUE;
        int flag35 = GLCapabilities.ARB_texture_buffer_range(ext) ? 0 : Integer.MIN_VALUE;
        int flag36 = GLCapabilities.ARB_texture_storage(ext) ? 0 : Integer.MIN_VALUE;
        int flag39 = GLCapabilities.ARB_texture_storage_multisample(ext) ? 0 : Integer.MIN_VALUE;
        int flag42 = GLCapabilities.ARB_vertex_array_object(ext) ? 0 : Integer.MIN_VALUE;
        int flag46 = GLCapabilities.ARB_vertex_attrib_binding(ext) ? 0 : Integer.MIN_VALUE;
        int flag47 = GLCapabilities.ARB_multi_bind(ext) ? 0 : Integer.MIN_VALUE;
        int flag56 = GLCapabilities.ARB_sampler_objects(ext) ? 0 : Integer.MIN_VALUE;
        int flag57 = GLCapabilities.ARB_separate_shader_objects(ext) ? 0 : Integer.MIN_VALUE;
        int flag58 = GLCapabilities.ARB_query_buffer_object(ext) ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{flag0 + 923, flag1 + 924, flag1 + 925, flag0 + 926, flag0 + 927, flag0 + 928, 929, flag6 + 930, 931, 932, flag7 + 933, flag8 + 934, flag8 + 935, 936, flag10 + 937, 938, flag10 + 939, 940, 941, 942, 943, flag12 + 944, flag12 + 945, flag14 + 946, flag12 + 947, flag12 + 948, flag12 + 949, flag12 + 950, flag12 + 951, flag20 + 952, flag20 + 953, flag12 + 954, flag12 + 955, flag12 + 956, flag12 + 957, flag12 + 958, flag12 + 959, flag14 + 960, flag12 + 961, flag12 + 962, flag12 + 963, flag12 + 964, flag12 + 965, 966, flag34 + 967, flag35 + 968, flag36 + 969, flag36 + 970, flag36 + 971, flag39 + 972, flag39 + 973, 974, 975, 976, 977, 978, 979, 980, 981, 982, 983, 984, 985, 986, 987, 988, flag12 + 989, 990, 991, 992, 993, 994, 995, 996, 997, 998, flag42 + 999, flag42 + 1000, flag42 + 1001, flag42 + 1002, flag46 + 1003, flag47 + 1004, flag46 + 1005, flag46 + 1006, flag46 + 1007, flag46 + 1008, flag46 + 1009, flag42 + 1010, flag42 + 1011, flag42 + 1012, flag56 + 1013, flag57 + 1014, 1015, flag58 + 1018, flag58 + 1016, flag58 + 1019, flag58 + 1017}, "glCreateTransformFeedbacks", "glTransformFeedbackBufferBase", "glTransformFeedbackBufferRange", "glGetTransformFeedbackiv", "glGetTransformFeedbacki_v", "glGetTransformFeedbacki64_v", "glCreateBuffers", "glNamedBufferStorage", "glNamedBufferData", "glNamedBufferSubData", "glCopyNamedBufferSubData", "glClearNamedBufferData", "glClearNamedBufferSubData", "glMapNamedBuffer", "glMapNamedBufferRange", "glUnmapNamedBuffer", "glFlushMappedNamedBufferRange", "glGetNamedBufferParameteriv", "glGetNamedBufferParameteri64v", "glGetNamedBufferPointerv", "glGetNamedBufferSubData", "glCreateFramebuffers", "glNamedFramebufferRenderbuffer", "glNamedFramebufferParameteri", "glNamedFramebufferTexture", "glNamedFramebufferTextureLayer", "glNamedFramebufferDrawBuffer", "glNamedFramebufferDrawBuffers", "glNamedFramebufferReadBuffer", "glInvalidateNamedFramebufferData", "glInvalidateNamedFramebufferSubData", "glClearNamedFramebufferiv", "glClearNamedFramebufferuiv", "glClearNamedFramebufferfv", "glClearNamedFramebufferfi", "glBlitNamedFramebuffer", "glCheckNamedFramebufferStatus", "glGetNamedFramebufferParameteriv", "glGetNamedFramebufferAttachmentParameteriv", "glCreateRenderbuffers", "glNamedRenderbufferStorage", "glNamedRenderbufferStorageMultisample", "glGetNamedRenderbufferParameteriv", "glCreateTextures", "glTextureBuffer", "glTextureBufferRange", "glTextureStorage1D", "glTextureStorage2D", "glTextureStorage3D", "glTextureStorage2DMultisample", "glTextureStorage3DMultisample", "glTextureSubImage1D", "glTextureSubImage2D", "glTextureSubImage3D", "glCompressedTextureSubImage1D", "glCompressedTextureSubImage2D", "glCompressedTextureSubImage3D", "glCopyTextureSubImage1D", "glCopyTextureSubImage2D", "glCopyTextureSubImage3D", "glTextureParameterf", "glTextureParameterfv", "glTextureParameteri", "glTextureParameterIiv", "glTextureParameterIuiv", "glTextureParameteriv", "glGenerateTextureMipmap", "glBindTextureUnit", "glGetTextureImage", "glGetCompressedTextureImage", "glGetTextureLevelParameterfv", "glGetTextureLevelParameteriv", "glGetTextureParameterfv", "glGetTextureParameterIiv", "glGetTextureParameterIuiv", "glGetTextureParameteriv", "glCreateVertexArrays", "glDisableVertexArrayAttrib", "glEnableVertexArrayAttrib", "glVertexArrayElementBuffer", "glVertexArrayVertexBuffer", "glVertexArrayVertexBuffers", "glVertexArrayAttribFormat", "glVertexArrayAttribIFormat", "glVertexArrayAttribLFormat", "glVertexArrayAttribBinding", "glVertexArrayBindingDivisor", "glGetVertexArrayiv", "glGetVertexArrayIndexediv", "glGetVertexArrayIndexed64iv", "glCreateSamplers", "glCreateProgramPipelines", "glCreateQueries", "glGetQueryBufferObjecti64v", "glGetQueryBufferObjectiv", "glGetQueryBufferObjectui64v", "glGetQueryBufferObjectuiv") || Checks.reportMissing("GL", "GL_ARB_direct_state_access");
    }

    private static boolean check_ARB_draw_buffers(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_draw_buffers")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1137}, "glDrawBuffersARB") || Checks.reportMissing("GL", "GL_ARB_draw_buffers");
    }

    private static boolean check_ARB_draw_buffers_blend(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_draw_buffers_blend")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1138, 1139, 1140, 1141}, "glBlendEquationiARB", "glBlendEquationSeparateiARB", "glBlendFunciARB", "glBlendFuncSeparateiARB") || Checks.reportMissing("GL", "GL_ARB_draw_buffers_blend");
    }

    private static boolean check_ARB_draw_elements_base_vertex(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_draw_elements_base_vertex")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{648, 649, 650, 651}, "glDrawElementsBaseVertex", "glDrawRangeElementsBaseVertex", "glDrawElementsInstancedBaseVertex", "glMultiDrawElementsBaseVertex") || Checks.reportMissing("GL", "GL_ARB_draw_elements_base_vertex");
    }

    private static boolean check_ARB_draw_indirect(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_draw_indirect")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{728, 729}, "glDrawArraysIndirect", "glDrawElementsIndirect") || Checks.reportMissing("GL", "GL_ARB_draw_indirect");
    }

    private static boolean check_ARB_draw_instanced(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_draw_instanced")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1142, 1143}, "glDrawArraysInstancedARB", "glDrawElementsInstancedARB") || Checks.reportMissing("GL", "GL_ARB_draw_instanced");
    }

    private static boolean check_ARB_ES2_compatibility(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_ES2_compatibility")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{770, 771, 772, 773, 774}, "glReleaseShaderCompiler", "glShaderBinary", "glGetShaderPrecisionFormat", "glDepthRangef", "glClearDepthf") || Checks.reportMissing("GL", "GL_ARB_ES2_compatibility");
    }

    private static boolean check_ARB_ES3_1_compatibility(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_ES3_1_compatibility")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1020}, "glMemoryBarrierByRegion") || Checks.reportMissing("GL", "GL_ARB_ES3_1_compatibility");
    }

    private static boolean check_ARB_ES3_2_compatibility(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_ES3_2_compatibility")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1144}, "glPrimitiveBoundingBoxARB") || Checks.reportMissing("GL", "GL_ARB_ES3_2_compatibility");
    }

    private static boolean check_ARB_framebuffer_no_attachments(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_framebuffer_no_attachments")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{885, 886, flag0 + 1145, flag0 + 1146}, "glFramebufferParameteri", "glGetFramebufferParameteriv", "glNamedFramebufferParameteriEXT", "glGetNamedFramebufferParameterivEXT") || Checks.reportMissing("GL", "GL_ARB_framebuffer_no_attachments");
    }

    private static boolean check_ARB_framebuffer_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_framebuffer_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{595, 596, 597, 598, 599, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 610, 611, 612, 613, 614}, "glIsRenderbuffer", "glBindRenderbuffer", "glDeleteRenderbuffers", "glGenRenderbuffers", "glRenderbufferStorage", "glRenderbufferStorageMultisample", "glGetRenderbufferParameteriv", "glIsFramebuffer", "glBindFramebuffer", "glDeleteFramebuffers", "glGenFramebuffers", "glCheckFramebufferStatus", "glFramebufferTexture1D", "glFramebufferTexture2D", "glFramebufferTexture3D", "glFramebufferTextureLayer", "glFramebufferRenderbuffer", "glGetFramebufferAttachmentParameteriv", "glBlitFramebuffer", "glGenerateMipmap") || Checks.reportMissing("GL", "GL_ARB_framebuffer_object");
    }

    private static boolean check_ARB_geometry_shader4(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_geometry_shader4")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1147, 1148, 1149, 1150}, "glProgramParameteriARB", "glFramebufferTextureARB", "glFramebufferTextureLayerARB", "glFramebufferTextureFaceARB") || Checks.reportMissing("GL", "GL_ARB_geometry_shader4");
    }

    private static boolean check_ARB_get_program_binary(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_get_program_binary")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{775, 776, 777}, "glGetProgramBinary", "glProgramBinary", "glProgramParameteri") || Checks.reportMissing("GL", "GL_ARB_get_program_binary");
    }

    private static boolean check_ARB_get_texture_sub_image(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_get_texture_sub_image")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1021, 1022}, "glGetTextureSubImage", "glGetCompressedTextureSubImage") || Checks.reportMissing("GL", "GL_ARB_get_texture_sub_image");
    }

    private static boolean check_ARB_gl_spirv(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_gl_spirv")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1151}, "glSpecializeShaderARB") || Checks.reportMissing("GL", "GL_ARB_gl_spirv");
    }

    private static boolean check_ARB_gpu_shader_fp64(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_gpu_shader_fp64")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{730, 731, 732, 733, 734, 735, 736, 737, 738, 739, 740, 741, 742, 743, 744, 745, 746, 747}, "glUniform1d", "glUniform2d", "glUniform3d", "glUniform4d", "glUniform1dv", "glUniform2dv", "glUniform3dv", "glUniform4dv", "glUniformMatrix2dv", "glUniformMatrix3dv", "glUniformMatrix4dv", "glUniformMatrix2x3dv", "glUniformMatrix2x4dv", "glUniformMatrix3x2dv", "glUniformMatrix3x4dv", "glUniformMatrix4x2dv", "glUniformMatrix4x3dv", "glGetUniformdv") || Checks.reportMissing("GL", "GL_ARB_gpu_shader_fp64");
    }

    private static boolean check_ARB_gpu_shader_int64(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_gpu_shader_int64")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181, 1182, 1183, 1184, 1185, 1186, 1187, 1188, 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197, 1198, 1199, 1200, 1201, 1202, 1203, 1204}, "glUniform1i64ARB", "glUniform1i64vARB", "glProgramUniform1i64ARB", "glProgramUniform1i64vARB", "glUniform2i64ARB", "glUniform2i64vARB", "glProgramUniform2i64ARB", "glProgramUniform2i64vARB", "glUniform3i64ARB", "glUniform3i64vARB", "glProgramUniform3i64ARB", "glProgramUniform3i64vARB", "glUniform4i64ARB", "glUniform4i64vARB", "glProgramUniform4i64ARB", "glProgramUniform4i64vARB", "glUniform1ui64ARB", "glUniform1ui64vARB", "glProgramUniform1ui64ARB", "glProgramUniform1ui64vARB", "glUniform2ui64ARB", "glUniform2ui64vARB", "glProgramUniform2ui64ARB", "glProgramUniform2ui64vARB", "glUniform3ui64ARB", "glUniform3ui64vARB", "glProgramUniform3ui64ARB", "glProgramUniform3ui64vARB", "glUniform4ui64ARB", "glUniform4ui64vARB", "glProgramUniform4ui64ARB", "glProgramUniform4ui64vARB", "glGetUniformi64vARB", "glGetUniformui64vARB", "glGetnUniformi64vARB", "glGetnUniformui64vARB") || Checks.reportMissing("GL", "GL_ARB_gpu_shader_int64");
    }

    private static boolean check_ARB_imaging(FunctionProvider provider, PointerBuffer caps, Set<String> ext, boolean fc) {
        if (!ext.contains("GL_ARB_imaging")) {
            return false;
        }
        return (fc || Checks.checkFunctions(provider, caps, new int[]{1205, 1206, 1207, 1208, 1209, 1210, 1211, 1212, 1213, 1214, 1215, 1216, 1217, 1218, 1219, 1220, 1221, 1222, 1223, 1224, 1225, 1226, 1227, 1228, 1229, 1230, 1231, 1232, 1233, 1234, 1235, 1236}, "glColorTable", "glCopyColorTable", "glColorTableParameteriv", "glColorTableParameterfv", "glGetColorTable", "glGetColorTableParameteriv", "glGetColorTableParameterfv", "glColorSubTable", "glCopyColorSubTable", "glConvolutionFilter1D", "glConvolutionFilter2D", "glCopyConvolutionFilter1D", "glCopyConvolutionFilter2D", "glGetConvolutionFilter", "glSeparableFilter2D", "glGetSeparableFilter", "glConvolutionParameteri", "glConvolutionParameteriv", "glConvolutionParameterf", "glConvolutionParameterfv", "glGetConvolutionParameteriv", "glGetConvolutionParameterfv", "glHistogram", "glResetHistogram", "glGetHistogram", "glGetHistogramParameteriv", "glGetHistogramParameterfv", "glMinmax", "glResetMinmax", "glGetMinmax", "glGetMinmaxParameteriv", "glGetMinmaxParameterfv")) && Checks.checkFunctions(provider, caps, new int[]{386, 387}, "glBlendColor", "glBlendEquation") || Checks.reportMissing("GL", "GL_ARB_imaging");
    }

    private static boolean check_ARB_indirect_parameters(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_indirect_parameters")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1237, 1238}, "glMultiDrawArraysIndirectCountARB", "glMultiDrawElementsIndirectCountARB") || Checks.reportMissing("GL", "GL_ARB_indirect_parameters");
    }

    private static boolean check_ARB_instanced_arrays(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_instanced_arrays")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1239}, "glVertexAttribDivisorARB") || Checks.reportMissing("GL", "GL_ARB_instanced_arrays");
    }

    private static boolean check_ARB_internalformat_query(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_internalformat_query")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{869}, "glGetInternalformativ") || Checks.reportMissing("GL", "GL_ARB_internalformat_query");
    }

    private static boolean check_ARB_internalformat_query2(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_internalformat_query2")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{887}, "glGetInternalformati64v") || Checks.reportMissing("GL", "GL_ARB_internalformat_query2");
    }

    private static boolean check_ARB_invalidate_subdata(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_invalidate_subdata")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{888, 889, 890, 891, 892, 893}, "glInvalidateTexSubImage", "glInvalidateTexImage", "glInvalidateBufferSubData", "glInvalidateBufferData", "glInvalidateFramebuffer", "glInvalidateSubFramebuffer") || Checks.reportMissing("GL", "GL_ARB_invalidate_subdata");
    }

    private static boolean check_ARB_map_buffer_range(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_map_buffer_range")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{592, 593}, "glMapBufferRange", "glFlushMappedBufferRange") || Checks.reportMissing("GL", "GL_ARB_map_buffer_range");
    }

    private static boolean check_ARB_matrix_palette(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_matrix_palette")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1241, 1242, 1243, 1244, 1245}, "glCurrentPaletteMatrixARB", "glMatrixIndexuivARB", "glMatrixIndexubvARB", "glMatrixIndexusvARB", "glMatrixIndexPointerARB") || Checks.reportMissing("GL", "GL_ARB_matrix_palette");
    }

    private static boolean check_ARB_multi_bind(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_multi_bind")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{916, 917, 918, 919, 920, 921}, "glBindBuffersBase", "glBindBuffersRange", "glBindTextures", "glBindSamplers", "glBindImageTextures", "glBindVertexBuffers") || Checks.reportMissing("GL", "GL_ARB_multi_bind");
    }

    private static boolean check_ARB_multi_draw_indirect(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_multi_draw_indirect")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{894, 895}, "glMultiDrawArraysIndirect", "glMultiDrawElementsIndirect") || Checks.reportMissing("GL", "GL_ARB_multi_draw_indirect");
    }

    private static boolean check_ARB_multisample(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_multisample")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1246}, "glSampleCoverageARB") || Checks.reportMissing("GL", "GL_ARB_multisample");
    }

    private static boolean check_ARB_multitexture(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_multitexture")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1247, 1248, 1249, 1250, 1251, 1252, 1253, 1254, 1255, 1256, 1257, 1258, 1259, 1260, 1261, 1262, 1263, 1264, 1265, 1266, 1267, 1268, 1269, 1270, 1271, 1272, 1273, 1274, 1275, 1276, 1277, 1278, 1279, 1280}, "glActiveTextureARB", "glClientActiveTextureARB", "glMultiTexCoord1fARB", "glMultiTexCoord1sARB", "glMultiTexCoord1iARB", "glMultiTexCoord1dARB", "glMultiTexCoord1fvARB", "glMultiTexCoord1svARB", "glMultiTexCoord1ivARB", "glMultiTexCoord1dvARB", "glMultiTexCoord2fARB", "glMultiTexCoord2sARB", "glMultiTexCoord2iARB", "glMultiTexCoord2dARB", "glMultiTexCoord2fvARB", "glMultiTexCoord2svARB", "glMultiTexCoord2ivARB", "glMultiTexCoord2dvARB", "glMultiTexCoord3fARB", "glMultiTexCoord3sARB", "glMultiTexCoord3iARB", "glMultiTexCoord3dARB", "glMultiTexCoord3fvARB", "glMultiTexCoord3svARB", "glMultiTexCoord3ivARB", "glMultiTexCoord3dvARB", "glMultiTexCoord4fARB", "glMultiTexCoord4sARB", "glMultiTexCoord4iARB", "glMultiTexCoord4dARB", "glMultiTexCoord4fvARB", "glMultiTexCoord4svARB", "glMultiTexCoord4ivARB", "glMultiTexCoord4dvARB") || Checks.reportMissing("GL", "GL_ARB_multitexture");
    }

    private static boolean check_ARB_occlusion_query(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_occlusion_query")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1281, 1282, 1283, 1284, 1285, 1286, 1287, 1288}, "glGenQueriesARB", "glDeleteQueriesARB", "glIsQueryARB", "glBeginQueryARB", "glEndQueryARB", "glGetQueryivARB", "glGetQueryObjectivARB", "glGetQueryObjectuivARB") || Checks.reportMissing("GL", "GL_ARB_occlusion_query");
    }

    private static boolean check_ARB_parallel_shader_compile(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_parallel_shader_compile")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1289}, "glMaxShaderCompilerThreadsARB") || Checks.reportMissing("GL", "GL_ARB_parallel_shader_compile");
    }

    private static boolean check_ARB_point_parameters(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_point_parameters")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1290, 1291}, "glPointParameterfARB", "glPointParameterfvARB") || Checks.reportMissing("GL", "GL_ARB_point_parameters");
    }

    private static boolean check_ARB_polygon_offset_clamp(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_polygon_offset_clamp")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1046}, "glPolygonOffsetClamp") || Checks.reportMissing("GL", "GL_ARB_polygon_offset_clamp");
    }

    private static boolean check_ARB_program_interface_query(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_program_interface_query")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{896, 897, 898, 899, 900, 901}, "glGetProgramInterfaceiv", "glGetProgramResourceIndex", "glGetProgramResourceName", "glGetProgramResourceiv", "glGetProgramResourceLocation", "glGetProgramResourceLocationIndex") || Checks.reportMissing("GL", "GL_ARB_program_interface_query");
    }

    private static boolean check_ARB_provoking_vertex(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_provoking_vertex")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{652}, "glProvokingVertex") || Checks.reportMissing("GL", "GL_ARB_provoking_vertex");
    }

    private static boolean check_ARB_robustness(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_robustness")) {
            return false;
        }
        int flag0 = provider.getFunctionAddress("glGetMapdv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag1 = provider.getFunctionAddress("glGetMapfv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag2 = provider.getFunctionAddress("glGetMapiv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag3 = provider.getFunctionAddress("glGetPixelMapfv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag4 = provider.getFunctionAddress("glGetPixelMapuiv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag5 = provider.getFunctionAddress("glGetPixelMapusv") != 0L ? 0 : Integer.MIN_VALUE;
        int flag6 = provider.getFunctionAddress("glGetPolygonStipple") != 0L ? 0 : Integer.MIN_VALUE;
        int flag7 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetColorTable") != 0L ? 0 : Integer.MIN_VALUE;
        int flag8 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetConvolutionFilter") != 0L ? 0 : Integer.MIN_VALUE;
        int flag9 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetSeparableFilter") != 0L ? 0 : Integer.MIN_VALUE;
        int flag10 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetHistogram") != 0L ? 0 : Integer.MIN_VALUE;
        int flag11 = ext.contains("GL_ARB_imaging") && provider.getFunctionAddress("glGetMinmax") != 0L ? 0 : Integer.MIN_VALUE;
        int flag12 = ext.contains("OpenGL13") ? 0 : Integer.MIN_VALUE;
        int flag13 = ext.contains("OpenGL20") ? 0 : Integer.MIN_VALUE;
        int flag15 = ext.contains("OpenGL30") ? 0 : Integer.MIN_VALUE;
        int flag16 = ext.contains("OpenGL40") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1292, flag0 + 1293, flag1 + 1294, flag2 + 1295, flag3 + 1296, flag4 + 1297, flag5 + 1298, flag6 + 1299, 1300, 1301, flag7 + 1302, flag8 + 1303, flag9 + 1304, flag10 + 1305, flag11 + 1306, flag12 + 1307, flag13 + 1308, flag13 + 1309, flag15 + 1310, flag16 + 1311}, "glGetGraphicsResetStatusARB", "glGetnMapdvARB", "glGetnMapfvARB", "glGetnMapivARB", "glGetnPixelMapfvARB", "glGetnPixelMapuivARB", "glGetnPixelMapusvARB", "glGetnPolygonStippleARB", "glGetnTexImageARB", "glReadnPixelsARB", "glGetnColorTableARB", "glGetnConvolutionFilterARB", "glGetnSeparableFilterARB", "glGetnHistogramARB", "glGetnMinmaxARB", "glGetnCompressedTexImageARB", "glGetnUniformfvARB", "glGetnUniformivARB", "glGetnUniformuivARB", "glGetnUniformdvARB") || Checks.reportMissing("GL", "GL_ARB_robustness");
    }

    private static boolean check_ARB_sample_locations(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_sample_locations")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1312, 1313, 1314}, "glFramebufferSampleLocationsfvARB", "glNamedFramebufferSampleLocationsfvARB", "glEvaluateDepthValuesARB") || Checks.reportMissing("GL", "GL_ARB_sample_locations");
    }

    private static boolean check_ARB_sample_shading(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_sample_shading")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1315}, "glMinSampleShadingARB") || Checks.reportMissing("GL", "GL_ARB_sample_shading");
    }

    private static boolean check_ARB_sampler_objects(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_sampler_objects")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{668, 669, 670, 671, 672, 673, 674, 675, 676, 677, 678, 679, 680, 681}, "glGenSamplers", "glDeleteSamplers", "glIsSampler", "glBindSampler", "glSamplerParameteri", "glSamplerParameterf", "glSamplerParameteriv", "glSamplerParameterfv", "glSamplerParameterIiv", "glSamplerParameterIuiv", "glGetSamplerParameteriv", "glGetSamplerParameterfv", "glGetSamplerParameterIiv", "glGetSamplerParameterIuiv") || Checks.reportMissing("GL", "GL_ARB_sampler_objects");
    }

    private static boolean check_ARB_separate_shader_objects(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_separate_shader_objects")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{778, 779, 780, 781, 782, 783, 784, 777, 785, 786, 787, 788, 789, 790, 791, 792, 793, 794, 795, 796, 797, 798, 799, 800, 801, 802, 803, 804, 805, 806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 816, 817, 818, 819, 820, 821, 822, 823, 824, 825, 826, 827, 828, 829, 830, 831, 832, 833, 834, 835, 836, 837}, "glUseProgramStages", "glActiveShaderProgram", "glCreateShaderProgramv", "glBindProgramPipeline", "glDeleteProgramPipelines", "glGenProgramPipelines", "glIsProgramPipeline", "glProgramParameteri", "glGetProgramPipelineiv", "glProgramUniform1i", "glProgramUniform2i", "glProgramUniform3i", "glProgramUniform4i", "glProgramUniform1ui", "glProgramUniform2ui", "glProgramUniform3ui", "glProgramUniform4ui", "glProgramUniform1f", "glProgramUniform2f", "glProgramUniform3f", "glProgramUniform4f", "glProgramUniform1d", "glProgramUniform2d", "glProgramUniform3d", "glProgramUniform4d", "glProgramUniform1iv", "glProgramUniform2iv", "glProgramUniform3iv", "glProgramUniform4iv", "glProgramUniform1uiv", "glProgramUniform2uiv", "glProgramUniform3uiv", "glProgramUniform4uiv", "glProgramUniform1fv", "glProgramUniform2fv", "glProgramUniform3fv", "glProgramUniform4fv", "glProgramUniform1dv", "glProgramUniform2dv", "glProgramUniform3dv", "glProgramUniform4dv", "glProgramUniformMatrix2fv", "glProgramUniformMatrix3fv", "glProgramUniformMatrix4fv", "glProgramUniformMatrix2dv", "glProgramUniformMatrix3dv", "glProgramUniformMatrix4dv", "glProgramUniformMatrix2x3fv", "glProgramUniformMatrix3x2fv", "glProgramUniformMatrix2x4fv", "glProgramUniformMatrix4x2fv", "glProgramUniformMatrix3x4fv", "glProgramUniformMatrix4x3fv", "glProgramUniformMatrix2x3dv", "glProgramUniformMatrix3x2dv", "glProgramUniformMatrix2x4dv", "glProgramUniformMatrix4x2dv", "glProgramUniformMatrix3x4dv", "glProgramUniformMatrix4x3dv", "glValidateProgramPipeline", "glGetProgramPipelineInfoLog") || Checks.reportMissing("GL", "GL_ARB_separate_shader_objects");
    }

    private static boolean check_ARB_shader_atomic_counters(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_shader_atomic_counters")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{858}, "glGetActiveAtomicCounterBufferiv") || Checks.reportMissing("GL", "GL_ARB_shader_atomic_counters");
    }

    private static boolean check_ARB_shader_image_load_store(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_shader_image_load_store")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{867, 868}, "glBindImageTexture", "glMemoryBarrier") || Checks.reportMissing("GL", "GL_ARB_shader_image_load_store");
    }

    private static boolean check_ARB_shader_objects(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_shader_objects")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1316, 1317, 1318, 1319, 1320, 1321, 1322, 1323, 1324, 1325, 1326, 1327, 1328, 1329, 1330, 1331, 1332, 1333, 1334, 1335, 1336, 1337, 1338, 1339, 1340, 1341, 1342, 1343, 1344, 1345, 1346, 1347, 1348, 1349, 1350, 1351, 1352, 1353, 1354}, "glDeleteObjectARB", "glGetHandleARB", "glDetachObjectARB", "glCreateShaderObjectARB", "glShaderSourceARB", "glCompileShaderARB", "glCreateProgramObjectARB", "glAttachObjectARB", "glLinkProgramARB", "glUseProgramObjectARB", "glValidateProgramARB", "glUniform1fARB", "glUniform2fARB", "glUniform3fARB", "glUniform4fARB", "glUniform1iARB", "glUniform2iARB", "glUniform3iARB", "glUniform4iARB", "glUniform1fvARB", "glUniform2fvARB", "glUniform3fvARB", "glUniform4fvARB", "glUniform1ivARB", "glUniform2ivARB", "glUniform3ivARB", "glUniform4ivARB", "glUniformMatrix2fvARB", "glUniformMatrix3fvARB", "glUniformMatrix4fvARB", "glGetObjectParameterfvARB", "glGetObjectParameterivARB", "glGetInfoLogARB", "glGetAttachedObjectsARB", "glGetUniformLocationARB", "glGetActiveUniformARB", "glGetUniformfvARB", "glGetUniformivARB", "glGetShaderSourceARB") || Checks.reportMissing("GL", "GL_ARB_shader_objects");
    }

    private static boolean check_ARB_shader_storage_buffer_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_shader_storage_buffer_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{902}, "glShaderStorageBlockBinding") || Checks.reportMissing("GL", "GL_ARB_shader_storage_buffer_object");
    }

    private static boolean check_ARB_shader_subroutine(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_shader_subroutine")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{749, 750, 751, 752, 753, 754, 755, 756}, "glGetSubroutineUniformLocation", "glGetSubroutineIndex", "glGetActiveSubroutineUniformiv", "glGetActiveSubroutineUniformName", "glGetActiveSubroutineName", "glUniformSubroutinesuiv", "glGetUniformSubroutineuiv", "glGetProgramStageiv") || Checks.reportMissing("GL", "GL_ARB_shader_subroutine");
    }

    private static boolean check_ARB_shading_language_include(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_shading_language_include")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1355, 1356, 1357, 1358, 1359, 1360}, "glNamedStringARB", "glDeleteNamedStringARB", "glCompileShaderIncludeARB", "glIsNamedStringARB", "glGetNamedStringARB", "glGetNamedStringivARB") || Checks.reportMissing("GL", "GL_ARB_shading_language_include");
    }

    private static boolean check_ARB_sparse_buffer(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_sparse_buffer")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        int flag1 = ext.contains("GL_ARB_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1361}, "glBufferPageCommitmentARB") || Checks.reportMissing("GL", "GL_ARB_sparse_buffer");
    }

    private static boolean check_ARB_sparse_texture(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_sparse_texture")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1364, flag0 + 1365}, "glTexPageCommitmentARB", "glTexturePageCommitmentEXT") || Checks.reportMissing("GL", "GL_ARB_sparse_texture");
    }

    private static boolean check_ARB_sync(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_sync")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{658, 659, 660, 661, 662, 663, 665}, "glFenceSync", "glIsSync", "glDeleteSync", "glClientWaitSync", "glWaitSync", "glGetInteger64v", "glGetSynciv") || Checks.reportMissing("GL", "GL_ARB_sync");
    }

    private static boolean check_ARB_tessellation_shader(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_tessellation_shader")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{757, 758}, "glPatchParameteri", "glPatchParameterfv") || Checks.reportMissing("GL", "GL_ARB_tessellation_shader");
    }

    private static boolean check_ARB_texture_barrier(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_texture_barrier")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1023}, "glTextureBarrier") || Checks.reportMissing("GL", "GL_ARB_texture_barrier");
    }

    private static boolean check_ARB_texture_buffer_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_texture_buffer_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1366}, "glTexBufferARB") || Checks.reportMissing("GL", "GL_ARB_texture_buffer_object");
    }

    private static boolean check_ARB_texture_buffer_range(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_texture_buffer_range")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{903, flag0 + 1367}, "glTexBufferRange", "glTextureBufferRangeEXT") || Checks.reportMissing("GL", "GL_ARB_texture_buffer_range");
    }

    private static boolean check_ARB_texture_compression(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_texture_compression")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1368, 1369, 1370, 1371, 1372, 1373, 1374}, "glCompressedTexImage3DARB", "glCompressedTexImage2DARB", "glCompressedTexImage1DARB", "glCompressedTexSubImage3DARB", "glCompressedTexSubImage2DARB", "glCompressedTexSubImage1DARB", "glGetCompressedTexImageARB") || Checks.reportMissing("GL", "GL_ARB_texture_compression");
    }

    private static boolean check_ARB_texture_multisample(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_texture_multisample")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{653, 654, 655, 656}, "glTexImage2DMultisample", "glTexImage3DMultisample", "glGetMultisamplefv", "glSampleMaski") || Checks.reportMissing("GL", "GL_ARB_texture_multisample");
    }

    private static boolean check_ARB_texture_storage(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_texture_storage")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{859, 860, 861, flag0 + 1375, flag0 + 1376, flag0 + 1377}, "glTexStorage1D", "glTexStorage2D", "glTexStorage3D", "glTextureStorage1DEXT", "glTextureStorage2DEXT", "glTextureStorage3DEXT") || Checks.reportMissing("GL", "GL_ARB_texture_storage");
    }

    private static boolean check_ARB_texture_storage_multisample(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_texture_storage_multisample")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{904, 905, flag0 + 1378, flag0 + 1379}, "glTexStorage2DMultisample", "glTexStorage3DMultisample", "glTextureStorage2DMultisampleEXT", "glTextureStorage3DMultisampleEXT") || Checks.reportMissing("GL", "GL_ARB_texture_storage_multisample");
    }

    private static boolean check_ARB_texture_view(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_texture_view")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{906}, "glTextureView") || Checks.reportMissing("GL", "GL_ARB_texture_view");
    }

    private static boolean check_ARB_timer_query(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_timer_query")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{682, 683, 684}, "glQueryCounter", "glGetQueryObjecti64v", "glGetQueryObjectui64v") || Checks.reportMissing("GL", "GL_ARB_timer_query");
    }

    private static boolean check_ARB_transform_feedback2(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_transform_feedback2")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{759, 760, 761, 762, 763, 764, 765}, "glBindTransformFeedback", "glDeleteTransformFeedbacks", "glGenTransformFeedbacks", "glIsTransformFeedback", "glPauseTransformFeedback", "glResumeTransformFeedback", "glDrawTransformFeedback") || Checks.reportMissing("GL", "GL_ARB_transform_feedback2");
    }

    private static boolean check_ARB_transform_feedback3(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_transform_feedback3")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{766, 767, 768, 769}, "glDrawTransformFeedbackStream", "glBeginQueryIndexed", "glEndQueryIndexed", "glGetQueryIndexediv") || Checks.reportMissing("GL", "GL_ARB_transform_feedback3");
    }

    private static boolean check_ARB_transform_feedback_instanced(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_transform_feedback_instanced")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{862, 863}, "glDrawTransformFeedbackInstanced", "glDrawTransformFeedbackStreamInstanced") || Checks.reportMissing("GL", "GL_ARB_transform_feedback_instanced");
    }

    private static boolean check_ARB_transpose_matrix(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_transpose_matrix")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1380, 1381, 1382, 1383}, "glLoadTransposeMatrixfARB", "glLoadTransposeMatrixdARB", "glMultTransposeMatrixfARB", "glMultTransposeMatrixdARB") || Checks.reportMissing("GL", "GL_ARB_transpose_matrix");
    }

    private static boolean check_ARB_uniform_buffer_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_uniform_buffer_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{640, 641, 642, 643, 644, 645, 625, 626, 621, 646}, "glGetUniformIndices", "glGetActiveUniformsiv", "glGetActiveUniformName", "glGetUniformBlockIndex", "glGetActiveUniformBlockiv", "glGetActiveUniformBlockName", "glBindBufferRange", "glBindBufferBase", "glGetIntegeri_v", "glUniformBlockBinding") || Checks.reportMissing("GL", "GL_ARB_uniform_buffer_object");
    }

    private static boolean check_ARB_vertex_array_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_vertex_array_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{631, 632, 633, 634}, "glBindVertexArray", "glDeleteVertexArrays", "glGenVertexArrays", "glIsVertexArray") || Checks.reportMissing("GL", "GL_ARB_vertex_array_object");
    }

    private static boolean check_ARB_vertex_attrib_64bit(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_vertex_attrib_64bit")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{838, 839, 840, 841, 842, 843, 844, 845, 846, 847, flag0 + 1384}, "glVertexAttribL1d", "glVertexAttribL2d", "glVertexAttribL3d", "glVertexAttribL4d", "glVertexAttribL1dv", "glVertexAttribL2dv", "glVertexAttribL3dv", "glVertexAttribL4dv", "glVertexAttribLPointer", "glGetVertexAttribLdv", "glVertexArrayVertexAttribLOffsetEXT") || Checks.reportMissing("GL", "GL_ARB_vertex_attrib_64bit");
    }

    private static boolean check_ARB_vertex_attrib_binding(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_vertex_attrib_binding")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{907, 908, 909, 910, 911, 912, flag0 + 1385, flag0 + 1386, flag0 + 1387, flag0 + 1388, flag0 + 1389, flag0 + 1390}, "glBindVertexBuffer", "glVertexAttribFormat", "glVertexAttribIFormat", "glVertexAttribLFormat", "glVertexAttribBinding", "glVertexBindingDivisor", "glVertexArrayBindVertexBufferEXT", "glVertexArrayVertexAttribFormatEXT", "glVertexArrayVertexAttribIFormatEXT", "glVertexArrayVertexAttribLFormatEXT", "glVertexArrayVertexAttribBindingEXT", "glVertexArrayVertexBindingDivisorEXT") || Checks.reportMissing("GL", "GL_ARB_vertex_attrib_binding");
    }

    private static boolean check_ARB_vertex_blend(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_vertex_blend")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1391, 1392, 1393, 1394, 1395, 1396, 1397, 1398, 1399, 1400}, "glWeightfvARB", "glWeightbvARB", "glWeightubvARB", "glWeightsvARB", "glWeightusvARB", "glWeightivARB", "glWeightuivARB", "glWeightdvARB", "glWeightPointerARB", "glVertexBlendARB") || Checks.reportMissing("GL", "GL_ARB_vertex_blend");
    }

    private static boolean check_ARB_vertex_buffer_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_vertex_buffer_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1401, 1402, 1403, 1404, 1405, 1406, 1407, 1408, 1409, 1410, 1411}, "glBindBufferARB", "glDeleteBuffersARB", "glGenBuffersARB", "glIsBufferARB", "glBufferDataARB", "glBufferSubDataARB", "glGetBufferSubDataARB", "glMapBufferARB", "glUnmapBufferARB", "glGetBufferParameterivARB", "glGetBufferPointervARB") || Checks.reportMissing("GL", "GL_ARB_vertex_buffer_object");
    }

    private static boolean check_ARB_vertex_program(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_vertex_program")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1412, 1413, 1414, 1415, 1416, 1417, 1418, 1419, 1420, 1421, 1422, 1423, 1424, 1425, 1426, 1427, 1428, 1429, 1430, 1431, 1432, 1433, 1434, 1435, 1436, 1437, 1438, 1439, 1440, 1441, 1442, 1443, 1444, 1445, 1446, 1447, 1448, 1449, 1450, 1451, 1452, 1453, 1454, 1455, 1456, 1457, 1458, 1459, 1460, 1461, 1462, 1463, 1464, 1465, 1466, 1467, 1468, 1469, 1470, 1471, 1472, 1473}, "glVertexAttrib1sARB", "glVertexAttrib1fARB", "glVertexAttrib1dARB", "glVertexAttrib2sARB", "glVertexAttrib2fARB", "glVertexAttrib2dARB", "glVertexAttrib3sARB", "glVertexAttrib3fARB", "glVertexAttrib3dARB", "glVertexAttrib4sARB", "glVertexAttrib4fARB", "glVertexAttrib4dARB", "glVertexAttrib4NubARB", "glVertexAttrib1svARB", "glVertexAttrib1fvARB", "glVertexAttrib1dvARB", "glVertexAttrib2svARB", "glVertexAttrib2fvARB", "glVertexAttrib2dvARB", "glVertexAttrib3svARB", "glVertexAttrib3fvARB", "glVertexAttrib3dvARB", "glVertexAttrib4fvARB", "glVertexAttrib4bvARB", "glVertexAttrib4svARB", "glVertexAttrib4ivARB", "glVertexAttrib4ubvARB", "glVertexAttrib4usvARB", "glVertexAttrib4uivARB", "glVertexAttrib4dvARB", "glVertexAttrib4NbvARB", "glVertexAttrib4NsvARB", "glVertexAttrib4NivARB", "glVertexAttrib4NubvARB", "glVertexAttrib4NusvARB", "glVertexAttrib4NuivARB", "glVertexAttribPointerARB", "glEnableVertexAttribArrayARB", "glDisableVertexAttribArrayARB", "glProgramStringARB", "glBindProgramARB", "glDeleteProgramsARB", "glGenProgramsARB", "glProgramEnvParameter4dARB", "glProgramEnvParameter4dvARB", "glProgramEnvParameter4fARB", "glProgramEnvParameter4fvARB", "glProgramLocalParameter4dARB", "glProgramLocalParameter4dvARB", "glProgramLocalParameter4fARB", "glProgramLocalParameter4fvARB", "glGetProgramEnvParameterfvARB", "glGetProgramEnvParameterdvARB", "glGetProgramLocalParameterfvARB", "glGetProgramLocalParameterdvARB", "glGetProgramivARB", "glGetProgramStringARB", "glGetVertexAttribfvARB", "glGetVertexAttribdvARB", "glGetVertexAttribivARB", "glGetVertexAttribPointervARB", "glIsProgramARB") || Checks.reportMissing("GL", "GL_ARB_vertex_program");
    }

    private static boolean check_ARB_vertex_shader(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_vertex_shader")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1413, 1412, 1414, 1416, 1415, 1417, 1419, 1418, 1420, 1422, 1421, 1423, 1424, 1426, 1425, 1427, 1429, 1428, 1430, 1432, 1431, 1433, 1434, 1436, 1441, 1437, 1435, 1438, 1439, 1440, 1442, 1443, 1444, 1445, 1446, 1447, 1448, 1449, 1450, 1474, 1475, 1476, 1471, 1469, 1470, 1472}, "glVertexAttrib1fARB", "glVertexAttrib1sARB", "glVertexAttrib1dARB", "glVertexAttrib2fARB", "glVertexAttrib2sARB", "glVertexAttrib2dARB", "glVertexAttrib3fARB", "glVertexAttrib3sARB", "glVertexAttrib3dARB", "glVertexAttrib4fARB", "glVertexAttrib4sARB", "glVertexAttrib4dARB", "glVertexAttrib4NubARB", "glVertexAttrib1fvARB", "glVertexAttrib1svARB", "glVertexAttrib1dvARB", "glVertexAttrib2fvARB", "glVertexAttrib2svARB", "glVertexAttrib2dvARB", "glVertexAttrib3fvARB", "glVertexAttrib3svARB", "glVertexAttrib3dvARB", "glVertexAttrib4fvARB", "glVertexAttrib4svARB", "glVertexAttrib4dvARB", "glVertexAttrib4ivARB", "glVertexAttrib4bvARB", "glVertexAttrib4ubvARB", "glVertexAttrib4usvARB", "glVertexAttrib4uivARB", "glVertexAttrib4NbvARB", "glVertexAttrib4NsvARB", "glVertexAttrib4NivARB", "glVertexAttrib4NubvARB", "glVertexAttrib4NusvARB", "glVertexAttrib4NuivARB", "glVertexAttribPointerARB", "glEnableVertexAttribArrayARB", "glDisableVertexAttribArrayARB", "glBindAttribLocationARB", "glGetActiveAttribARB", "glGetAttribLocationARB", "glGetVertexAttribivARB", "glGetVertexAttribfvARB", "glGetVertexAttribdvARB", "glGetVertexAttribPointervARB") || Checks.reportMissing("GL", "GL_ARB_vertex_shader");
    }

    private static boolean check_ARB_vertex_type_2_10_10_10_rev(FunctionProvider provider, PointerBuffer caps, Set<String> ext, boolean fc) {
        if (!ext.contains("GL_ARB_vertex_type_2_10_10_10_rev")) {
            return false;
        }
        return (fc || Checks.checkFunctions(provider, caps, new int[]{686, 687, 688, 689, 690, 691, 692, 693, 694, 695, 696, 697, 698, 699, 700, 701, 702, 703, 704, 705, 706, 707, 708, 709, 710, 711, 712, 713, 714, 715}, "glVertexP2ui", "glVertexP3ui", "glVertexP4ui", "glVertexP2uiv", "glVertexP3uiv", "glVertexP4uiv", "glTexCoordP1ui", "glTexCoordP2ui", "glTexCoordP3ui", "glTexCoordP4ui", "glTexCoordP1uiv", "glTexCoordP2uiv", "glTexCoordP3uiv", "glTexCoordP4uiv", "glMultiTexCoordP1ui", "glMultiTexCoordP2ui", "glMultiTexCoordP3ui", "glMultiTexCoordP4ui", "glMultiTexCoordP1uiv", "glMultiTexCoordP2uiv", "glMultiTexCoordP3uiv", "glMultiTexCoordP4uiv", "glNormalP3ui", "glNormalP3uiv", "glColorP3ui", "glColorP4ui", "glColorP3uiv", "glColorP4uiv", "glSecondaryColorP3ui", "glSecondaryColorP3uiv")) && Checks.checkFunctions(provider, caps, new int[]{716, 717, 718, 719, 720, 721, 722, 723}, "glVertexAttribP1ui", "glVertexAttribP2ui", "glVertexAttribP3ui", "glVertexAttribP4ui", "glVertexAttribP1uiv", "glVertexAttribP2uiv", "glVertexAttribP3uiv", "glVertexAttribP4uiv") || Checks.reportMissing("GL", "GL_ARB_vertex_type_2_10_10_10_rev");
    }

    private static boolean check_ARB_viewport_array(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_viewport_array")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{848, 849, 850, 851, 852, 853, 854, 855, 856, 857}, "glViewportArrayv", "glViewportIndexedf", "glViewportIndexedfv", "glScissorArrayv", "glScissorIndexed", "glScissorIndexedv", "glDepthRangeArrayv", "glDepthRangeIndexed", "glGetFloati_v", "glGetDoublei_v") || Checks.reportMissing("GL", "GL_ARB_viewport_array");
    }

    private static boolean check_ARB_window_pos(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_ARB_window_pos")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1477, 1478, 1479, 1480, 1481, 1482, 1483, 1484, 1485, 1486, 1487, 1488, 1489, 1490, 1491, 1492}, "glWindowPos2iARB", "glWindowPos2sARB", "glWindowPos2fARB", "glWindowPos2dARB", "glWindowPos2ivARB", "glWindowPos2svARB", "glWindowPos2fvARB", "glWindowPos2dvARB", "glWindowPos3iARB", "glWindowPos3sARB", "glWindowPos3fARB", "glWindowPos3dARB", "glWindowPos3ivARB", "glWindowPos3svARB", "glWindowPos3fvARB", "glWindowPos3dvARB") || Checks.reportMissing("GL", "GL_ARB_window_pos");
    }

    private static boolean check_EXT_bindable_uniform(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_bindable_uniform")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1493, 1494, 1495}, "glUniformBufferEXT", "glGetUniformBufferSizeEXT", "glGetUniformOffsetEXT") || Checks.reportMissing("GL", "GL_EXT_bindable_uniform");
    }

    private static boolean check_EXT_blend_color(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_blend_color")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1496}, "glBlendColorEXT") || Checks.reportMissing("GL", "GL_EXT_blend_color");
    }

    private static boolean check_EXT_blend_equation_separate(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_blend_equation_separate")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1497}, "glBlendEquationSeparateEXT") || Checks.reportMissing("GL", "GL_EXT_blend_equation_separate");
    }

    private static boolean check_EXT_blend_func_separate(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_blend_func_separate")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1498}, "glBlendFuncSeparateEXT") || Checks.reportMissing("GL", "GL_EXT_blend_func_separate");
    }

    private static boolean check_EXT_blend_minmax(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_blend_minmax")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1499}, "glBlendEquationEXT") || Checks.reportMissing("GL", "GL_EXT_blend_minmax");
    }

    private static boolean check_EXT_compiled_vertex_array(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_compiled_vertex_array")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1500, 1501}, "glLockArraysEXT", "glUnlockArraysEXT") || Checks.reportMissing("GL", "GL_EXT_compiled_vertex_array");
    }

    private static boolean check_EXT_debug_label(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_debug_label")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1502, 1503}, "glLabelObjectEXT", "glGetObjectLabelEXT") || Checks.reportMissing("GL", "GL_EXT_debug_label");
    }

    private static boolean check_EXT_debug_marker(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_debug_marker")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1504, 1505, 1506}, "glInsertEventMarkerEXT", "glPushGroupMarkerEXT", "glPopGroupMarkerEXT") || Checks.reportMissing("GL", "GL_EXT_debug_marker");
    }

    private static boolean check_EXT_depth_bounds_test(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_depth_bounds_test")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1507}, "glDepthBoundsEXT") || Checks.reportMissing("GL", "GL_EXT_depth_bounds_test");
    }

    private static boolean check_EXT_direct_state_access(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_direct_state_access")) {
            return false;
        }
        int flag0 = ext.contains("OpenGL12") ? 0 : Integer.MIN_VALUE;
        int flag3 = ext.contains("OpenGL13") ? 0 : Integer.MIN_VALUE;
        int flag42 = ext.contains("OpenGL30") ? 0 : Integer.MIN_VALUE;
        int flag55 = ext.contains("GL_ARB_vertex_program") ? 0 : Integer.MIN_VALUE;
        int flag82 = ext.contains("OpenGL15") ? 0 : Integer.MIN_VALUE;
        int flag88 = ext.contains("OpenGL20") ? 0 : Integer.MIN_VALUE;
        int flag107 = ext.contains("OpenGL21") ? 0 : Integer.MIN_VALUE;
        int flag113 = ext.contains("GL_EXT_texture_buffer_object") ? 0 : Integer.MIN_VALUE;
        int flag115 = ext.contains("GL_EXT_texture_integer") ? 0 : Integer.MIN_VALUE;
        int flag123 = ext.contains("GL_EXT_gpu_shader4") ? 0 : Integer.MIN_VALUE;
        int flag131 = ext.contains("GL_EXT_gpu_program_parameters") ? 0 : Integer.MIN_VALUE;
        int flag132 = ext.contains("GL_NV_gpu_program4") ? 0 : Integer.MIN_VALUE;
        int flag143 = ext.contains("GL_NV_framebuffer_multisample_coverage") ? 0 : Integer.MIN_VALUE;
        int flag157 = ext.contains("GL_EXT_geometry_shader4") || ext.contains("GL_NV_gpu_program4") ? 0 : Integer.MIN_VALUE;
        int flag160 = ext.contains("GL_NV_explicit_multisample") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1508, 1509, 1510, 1511, 1512, 1513, 1514, 1515, 1516, 1517, 1518, 1519, 1520, 1521, 1522, 1523, 1524, 1525, 1526, 1527, 1528, 1529, 1530, 1531, 1532, 1533, 1534, 1535, 1536, 1537, 1538, 1539, 1540, 1541, flag0 + 1542, flag0 + 1543, flag0 + 1544, flag3 + 1545, flag3 + 1546, flag3 + 1547, flag3 + 1548, flag3 + 1549, flag3 + 1550, flag3 + 1551, flag3 + 1552, flag3 + 1553, flag3 + 1554, flag3 + 1555, flag3 + 1556, flag3 + 1557, flag3 + 1558, flag3 + 1559, flag3 + 1560, flag3 + 1561, flag3 + 1562, flag3 + 1563, flag3 + 1564, flag3 + 1565, flag3 + 1566, flag3 + 1567, flag3 + 1568, flag3 + 1569, flag3 + 1570, flag3 + 1571, flag3 + 1572, flag3 + 1573, flag3 + 1574, flag3 + 1575, flag3 + 1576, flag3 + 1577, flag3 + 1578, flag3 + 1579, flag3 + 1580, flag3 + 1581, flag3 + 1582, flag3 + 1583, flag3 + 1586, flag3 + 1587, flag3 + 1588, flag3 + 1592, flag3 + 1593, flag3 + 1594, flag3 + 1595, flag3 + 1596, flag55 + 1597, flag55 + 1598, flag55 + 1599, flag55 + 1600, flag55 + 1601, flag55 + 1602, flag55 + 1603, flag55 + 1604, flag55 + 1605, flag3 + 1606, flag3 + 1607, flag3 + 1608, flag3 + 1609, flag3 + 1610, flag3 + 1611, flag3 + 1612, flag3 + 1613, flag3 + 1614, flag3 + 1615, flag3 + 1616, flag3 + 1617, flag3 + 1618, flag3 + 1619, flag3 + 1620, flag3 + 1621, flag3 + 1622, flag3 + 1623, flag82 + 1624, flag82 + 1625, flag82 + 1626, flag82 + 1627, flag82 + 1628, flag82 + 1629, flag88 + 1630, flag88 + 1631, flag88 + 1632, flag88 + 1633, flag88 + 1634, flag88 + 1635, flag88 + 1636, flag88 + 1637, flag88 + 1638, flag88 + 1639, flag88 + 1640, flag88 + 1641, flag88 + 1642, flag88 + 1643, flag88 + 1644, flag88 + 1645, flag88 + 1646, flag88 + 1647, flag88 + 1648, flag107 + 1649, flag107 + 1650, flag107 + 1651, flag107 + 1652, flag107 + 1653, flag107 + 1654, flag113 + 1655, flag113 + 1656, flag115 + 1657, flag115 + 1658, flag115 + 1659, flag115 + 1660, flag115 + 1661, flag115 + 1662, flag115 + 1663, flag115 + 1664, flag123 + 1665, flag123 + 1666, flag123 + 1667, flag123 + 1668, flag123 + 1669, flag123 + 1670, flag123 + 1671, flag123 + 1672, flag131 + 1673, flag132 + 1674, flag132 + 1675, flag132 + 1676, flag132 + 1677, flag132 + 1678, flag132 + 1679, flag132 + 1680, flag132 + 1681, flag42 + 1682, flag42 + 1683, flag42 + 1684, flag143 + 1685, flag42 + 1686, flag42 + 1687, flag42 + 1688, flag42 + 1689, flag42 + 1690, flag42 + 1691, flag42 + 1692, flag42 + 1693, flag42 + 1694, flag42 + 1695, flag42 + 1696, flag42 + 1697, flag42 + 1698, flag157 + 1699, flag157 + 1700, flag157 + 1701, flag160 + 1702, flag160 + 1703, flag42 + 1704, flag42 + 1705, flag42 + 1706, flag42 + 1707, flag42 + 1708, flag42 + 1709, flag42 + 1710, flag42 + 1711, flag42 + 1712, flag42 + 1713, flag42 + 1714, flag42 + 1715, flag42 + 1716, flag42 + 1717, flag42 + 1718, flag42 + 1719, flag42 + 1720, flag42 + 1721, flag42 + 1722, flag42 + 1723, flag42 + 1724}, "glClientAttribDefaultEXT", "glPushClientAttribDefaultEXT", "glMatrixLoadfEXT", "glMatrixLoaddEXT", "glMatrixMultfEXT", "glMatrixMultdEXT", "glMatrixLoadIdentityEXT", "glMatrixRotatefEXT", "glMatrixRotatedEXT", "glMatrixScalefEXT", "glMatrixScaledEXT", "glMatrixTranslatefEXT", "glMatrixTranslatedEXT", "glMatrixOrthoEXT", "glMatrixFrustumEXT", "glMatrixPushEXT", "glMatrixPopEXT", "glTextureParameteriEXT", "glTextureParameterivEXT", "glTextureParameterfEXT", "glTextureParameterfvEXT", "glTextureImage1DEXT", "glTextureImage2DEXT", "glTextureSubImage1DEXT", "glTextureSubImage2DEXT", "glCopyTextureImage1DEXT", "glCopyTextureImage2DEXT", "glCopyTextureSubImage1DEXT", "glCopyTextureSubImage2DEXT", "glGetTextureImageEXT", "glGetTextureParameterfvEXT", "glGetTextureParameterivEXT", "glGetTextureLevelParameterfvEXT", "glGetTextureLevelParameterivEXT", "glTextureImage3DEXT", "glTextureSubImage3DEXT", "glCopyTextureSubImage3DEXT", "glBindMultiTextureEXT", "glMultiTexCoordPointerEXT", "glMultiTexEnvfEXT", "glMultiTexEnvfvEXT", "glMultiTexEnviEXT", "glMultiTexEnvivEXT", "glMultiTexGendEXT", "glMultiTexGendvEXT", "glMultiTexGenfEXT", "glMultiTexGenfvEXT", "glMultiTexGeniEXT", "glMultiTexGenivEXT", "glGetMultiTexEnvfvEXT", "glGetMultiTexEnvivEXT", "glGetMultiTexGendvEXT", "glGetMultiTexGenfvEXT", "glGetMultiTexGenivEXT", "glMultiTexParameteriEXT", "glMultiTexParameterivEXT", "glMultiTexParameterfEXT", "glMultiTexParameterfvEXT", "glMultiTexImage1DEXT", "glMultiTexImage2DEXT", "glMultiTexSubImage1DEXT", "glMultiTexSubImage2DEXT", "glCopyMultiTexImage1DEXT", "glCopyMultiTexImage2DEXT", "glCopyMultiTexSubImage1DEXT", "glCopyMultiTexSubImage2DEXT", "glGetMultiTexImageEXT", "glGetMultiTexParameterfvEXT", "glGetMultiTexParameterivEXT", "glGetMultiTexLevelParameterfvEXT", "glGetMultiTexLevelParameterivEXT", "glMultiTexImage3DEXT", "glMultiTexSubImage3DEXT", "glCopyMultiTexSubImage3DEXT", "glEnableClientStateIndexedEXT", "glDisableClientStateIndexedEXT", "glGetFloatIndexedvEXT", "glGetDoubleIndexedvEXT", "glGetPointerIndexedvEXT", "glEnableIndexedEXT", "glDisableIndexedEXT", "glIsEnabledIndexedEXT", "glGetIntegerIndexedvEXT", "glGetBooleanIndexedvEXT", "glNamedProgramStringEXT", "glNamedProgramLocalParameter4dEXT", "glNamedProgramLocalParameter4dvEXT", "glNamedProgramLocalParameter4fEXT", "glNamedProgramLocalParameter4fvEXT", "glGetNamedProgramLocalParameterdvEXT", "glGetNamedProgramLocalParameterfvEXT", "glGetNamedProgramivEXT", "glGetNamedProgramStringEXT", "glCompressedTextureImage3DEXT", "glCompressedTextureImage2DEXT", "glCompressedTextureImage1DEXT", "glCompressedTextureSubImage3DEXT", "glCompressedTextureSubImage2DEXT", "glCompressedTextureSubImage1DEXT", "glGetCompressedTextureImageEXT", "glCompressedMultiTexImage3DEXT", "glCompressedMultiTexImage2DEXT", "glCompressedMultiTexImage1DEXT", "glCompressedMultiTexSubImage3DEXT", "glCompressedMultiTexSubImage2DEXT", "glCompressedMultiTexSubImage1DEXT", "glGetCompressedMultiTexImageEXT", "glMatrixLoadTransposefEXT", "glMatrixLoadTransposedEXT", "glMatrixMultTransposefEXT", "glMatrixMultTransposedEXT", "glNamedBufferDataEXT", "glNamedBufferSubDataEXT", "glMapNamedBufferEXT", "glUnmapNamedBufferEXT", "glGetNamedBufferParameterivEXT", "glGetNamedBufferSubDataEXT", "glProgramUniform1fEXT", "glProgramUniform2fEXT", "glProgramUniform3fEXT", "glProgramUniform4fEXT", "glProgramUniform1iEXT", "glProgramUniform2iEXT", "glProgramUniform3iEXT", "glProgramUniform4iEXT", "glProgramUniform1fvEXT", "glProgramUniform2fvEXT", "glProgramUniform3fvEXT", "glProgramUniform4fvEXT", "glProgramUniform1ivEXT", "glProgramUniform2ivEXT", "glProgramUniform3ivEXT", "glProgramUniform4ivEXT", "glProgramUniformMatrix2fvEXT", "glProgramUniformMatrix3fvEXT", "glProgramUniformMatrix4fvEXT", "glProgramUniformMatrix2x3fvEXT", "glProgramUniformMatrix3x2fvEXT", "glProgramUniformMatrix2x4fvEXT", "glProgramUniformMatrix4x2fvEXT", "glProgramUniformMatrix3x4fvEXT", "glProgramUniformMatrix4x3fvEXT", "glTextureBufferEXT", "glMultiTexBufferEXT", "glTextureParameterIivEXT", "glTextureParameterIuivEXT", "glGetTextureParameterIivEXT", "glGetTextureParameterIuivEXT", "glMultiTexParameterIivEXT", "glMultiTexParameterIuivEXT", "glGetMultiTexParameterIivEXT", "glGetMultiTexParameterIuivEXT", "glProgramUniform1uiEXT", "glProgramUniform2uiEXT", "glProgramUniform3uiEXT", "glProgramUniform4uiEXT", "glProgramUniform1uivEXT", "glProgramUniform2uivEXT", "glProgramUniform3uivEXT", "glProgramUniform4uivEXT", "glNamedProgramLocalParameters4fvEXT", "glNamedProgramLocalParameterI4iEXT", "glNamedProgramLocalParameterI4ivEXT", "glNamedProgramLocalParametersI4ivEXT", "glNamedProgramLocalParameterI4uiEXT", "glNamedProgramLocalParameterI4uivEXT", "glNamedProgramLocalParametersI4uivEXT", "glGetNamedProgramLocalParameterIivEXT", "glGetNamedProgramLocalParameterIuivEXT", "glNamedRenderbufferStorageEXT", "glGetNamedRenderbufferParameterivEXT", "glNamedRenderbufferStorageMultisampleEXT", "glNamedRenderbufferStorageMultisampleCoverageEXT", "glCheckNamedFramebufferStatusEXT", "glNamedFramebufferTexture1DEXT", "glNamedFramebufferTexture2DEXT", "glNamedFramebufferTexture3DEXT", "glNamedFramebufferRenderbufferEXT", "glGetNamedFramebufferAttachmentParameterivEXT", "glGenerateTextureMipmapEXT", "glGenerateMultiTexMipmapEXT", "glFramebufferDrawBufferEXT", "glFramebufferDrawBuffersEXT", "glFramebufferReadBufferEXT", "glGetFramebufferParameterivEXT", "glNamedCopyBufferSubDataEXT", "glNamedFramebufferTextureEXT", "glNamedFramebufferTextureLayerEXT", "glNamedFramebufferTextureFaceEXT", "glTextureRenderbufferEXT", "glMultiTexRenderbufferEXT", "glVertexArrayVertexOffsetEXT", "glVertexArrayColorOffsetEXT", "glVertexArrayEdgeFlagOffsetEXT", "glVertexArrayIndexOffsetEXT", "glVertexArrayNormalOffsetEXT", "glVertexArrayTexCoordOffsetEXT", "glVertexArrayMultiTexCoordOffsetEXT", "glVertexArrayFogCoordOffsetEXT", "glVertexArraySecondaryColorOffsetEXT", "glVertexArrayVertexAttribOffsetEXT", "glVertexArrayVertexAttribIOffsetEXT", "glEnableVertexArrayEXT", "glDisableVertexArrayEXT", "glEnableVertexArrayAttribEXT", "glDisableVertexArrayAttribEXT", "glGetVertexArrayIntegervEXT", "glGetVertexArrayPointervEXT", "glGetVertexArrayIntegeri_vEXT", "glGetVertexArrayPointeri_vEXT", "glMapNamedBufferRangeEXT", "glFlushMappedNamedBufferRangeEXT") || Checks.reportMissing("GL", "GL_EXT_direct_state_access");
    }

    private static boolean check_EXT_draw_buffers2(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_draw_buffers2")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1725, 1596, 1595, 1592, 1593, 1594}, "glColorMaskIndexedEXT", "glGetBooleanIndexedvEXT", "glGetIntegerIndexedvEXT", "glEnableIndexedEXT", "glDisableIndexedEXT", "glIsEnabledIndexedEXT") || Checks.reportMissing("GL", "GL_EXT_draw_buffers2");
    }

    private static boolean check_EXT_draw_instanced(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_draw_instanced")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1726, 1727}, "glDrawArraysInstancedEXT", "glDrawElementsInstancedEXT") || Checks.reportMissing("GL", "GL_EXT_draw_instanced");
    }

    private static boolean check_EXT_EGL_image_storage(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_EGL_image_storage")) {
            return false;
        }
        int flag0 = GLCapabilities.hasDSA(ext) ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1728, flag0 + 1729}, "glEGLImageTargetTexStorageEXT", "glEGLImageTargetTextureStorageEXT") || Checks.reportMissing("GL", "GL_EXT_EGL_image_storage");
    }

    private static boolean check_EXT_external_buffer(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_external_buffer")) {
            return false;
        }
        int flag0 = GLCapabilities.hasDSA(ext) ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1730, flag0 + 1731}, "glBufferStorageExternalEXT", "glNamedBufferStorageExternalEXT") || Checks.reportMissing("GL", "GL_EXT_external_buffer");
    }

    private static boolean check_EXT_framebuffer_blit(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_framebuffer_blit")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1732}, "glBlitFramebufferEXT") || Checks.reportMissing("GL", "GL_EXT_framebuffer_blit");
    }

    private static boolean check_EXT_framebuffer_blit_layers(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_framebuffer_blit_layers")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1733, 1734}, "glBlitFramebufferLayersEXT", "glBlitFramebufferLayerEXT") || Checks.reportMissing("GL", "GL_EXT_framebuffer_blit_layers");
    }

    private static boolean check_EXT_framebuffer_multisample(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_framebuffer_multisample")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1735}, "glRenderbufferStorageMultisampleEXT") || Checks.reportMissing("GL", "GL_EXT_framebuffer_multisample");
    }

    private static boolean check_EXT_framebuffer_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_framebuffer_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1736, 1737, 1738, 1739, 1740, 1741, 1742, 1743, 1744, 1745, 1746, 1747, 1748, 1749, 1750, 1751, 1752}, "glIsRenderbufferEXT", "glBindRenderbufferEXT", "glDeleteRenderbuffersEXT", "glGenRenderbuffersEXT", "glRenderbufferStorageEXT", "glGetRenderbufferParameterivEXT", "glIsFramebufferEXT", "glBindFramebufferEXT", "glDeleteFramebuffersEXT", "glGenFramebuffersEXT", "glCheckFramebufferStatusEXT", "glFramebufferTexture1DEXT", "glFramebufferTexture2DEXT", "glFramebufferTexture3DEXT", "glFramebufferRenderbufferEXT", "glGetFramebufferAttachmentParameterivEXT", "glGenerateMipmapEXT") || Checks.reportMissing("GL", "GL_EXT_framebuffer_object");
    }

    private static boolean check_EXT_geometry_shader4(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_geometry_shader4")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1753, 1754, 1755, 1756}, "glProgramParameteriEXT", "glFramebufferTextureEXT", "glFramebufferTextureLayerEXT", "glFramebufferTextureFaceEXT") || Checks.reportMissing("GL", "GL_EXT_geometry_shader4");
    }

    private static boolean check_EXT_gpu_program_parameters(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_gpu_program_parameters")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1757, 1758}, "glProgramEnvParameters4fvEXT", "glProgramLocalParameters4fvEXT") || Checks.reportMissing("GL", "GL_EXT_gpu_program_parameters");
    }

    private static boolean check_EXT_gpu_shader4(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_gpu_shader4")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1759, 1760, 1761, 1762, 1763, 1764, 1765, 1766, 1767, 1768, 1769, 1770, 1771, 1772, 1773, 1774, 1775, 1776, 1777, 1778, 1779, 1780, 1781, 1782, 1783, 1784, 1785, 1786, 1787, 1788, 1789, 1790, 1791, 1792}, "glVertexAttribI1iEXT", "glVertexAttribI2iEXT", "glVertexAttribI3iEXT", "glVertexAttribI4iEXT", "glVertexAttribI1uiEXT", "glVertexAttribI2uiEXT", "glVertexAttribI3uiEXT", "glVertexAttribI4uiEXT", "glVertexAttribI1ivEXT", "glVertexAttribI2ivEXT", "glVertexAttribI3ivEXT", "glVertexAttribI4ivEXT", "glVertexAttribI1uivEXT", "glVertexAttribI2uivEXT", "glVertexAttribI3uivEXT", "glVertexAttribI4uivEXT", "glVertexAttribI4bvEXT", "glVertexAttribI4svEXT", "glVertexAttribI4ubvEXT", "glVertexAttribI4usvEXT", "glVertexAttribIPointerEXT", "glGetVertexAttribIivEXT", "glGetVertexAttribIuivEXT", "glGetUniformuivEXT", "glBindFragDataLocationEXT", "glGetFragDataLocationEXT", "glUniform1uiEXT", "glUniform2uiEXT", "glUniform3uiEXT", "glUniform4uiEXT", "glUniform1uivEXT", "glUniform2uivEXT", "glUniform3uivEXT", "glUniform4uivEXT") || Checks.reportMissing("GL", "GL_EXT_gpu_shader4");
    }

    private static boolean check_EXT_memory_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_memory_object")) {
            return false;
        }
        int flag0 = GLCapabilities.hasDSA(ext) ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1793, 1794, 1795, 1796, 1797, 1798, 1799, 1800, 1801, 1802, 1803, 1804, flag0 + 1805, flag0 + 1806, flag0 + 1807, flag0 + 1808, flag0 + 1809, 1810, flag0 + 1811}, "glGetUnsignedBytevEXT", "glGetUnsignedBytei_vEXT", "glDeleteMemoryObjectsEXT", "glIsMemoryObjectEXT", "glCreateMemoryObjectsEXT", "glMemoryObjectParameterivEXT", "glGetMemoryObjectParameterivEXT", "glTexStorageMem2DEXT", "glTexStorageMem2DMultisampleEXT", "glTexStorageMem3DEXT", "glTexStorageMem3DMultisampleEXT", "glBufferStorageMemEXT", "glTextureStorageMem2DEXT", "glTextureStorageMem2DMultisampleEXT", "glTextureStorageMem3DEXT", "glTextureStorageMem3DMultisampleEXT", "glNamedBufferStorageMemEXT", "glTexStorageMem1DEXT", "glTextureStorageMem1DEXT") || Checks.reportMissing("GL", "GL_EXT_memory_object");
    }

    private static boolean check_EXT_memory_object_fd(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_memory_object_fd")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1812}, "glImportMemoryFdEXT") || Checks.reportMissing("GL", "GL_EXT_memory_object_fd");
    }

    private static boolean check_EXT_memory_object_win32(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_memory_object_win32")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1813, 1814}, "glImportMemoryWin32HandleEXT", "glImportMemoryWin32NameEXT") || Checks.reportMissing("GL", "GL_EXT_memory_object_win32");
    }

    private static boolean check_EXT_point_parameters(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_point_parameters")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1815, 1816}, "glPointParameterfEXT", "glPointParameterfvEXT") || Checks.reportMissing("GL", "GL_EXT_point_parameters");
    }

    private static boolean check_EXT_polygon_offset_clamp(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_polygon_offset_clamp")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1817}, "glPolygonOffsetClampEXT") || Checks.reportMissing("GL", "GL_EXT_polygon_offset_clamp");
    }

    private static boolean check_EXT_provoking_vertex(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_provoking_vertex")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1818}, "glProvokingVertexEXT") || Checks.reportMissing("GL", "GL_EXT_provoking_vertex");
    }

    private static boolean check_EXT_raster_multisample(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_raster_multisample")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1819}, "glRasterSamplesEXT") || Checks.reportMissing("GL", "GL_EXT_raster_multisample");
    }

    private static boolean check_EXT_secondary_color(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_secondary_color")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1820, 1821, 1822, 1823, 1824, 1825, 1826, 1827, 1828, 1829, 1830, 1831, 1832, 1833, 1834, 1835, 1836}, "glSecondaryColor3bEXT", "glSecondaryColor3sEXT", "glSecondaryColor3iEXT", "glSecondaryColor3fEXT", "glSecondaryColor3dEXT", "glSecondaryColor3ubEXT", "glSecondaryColor3usEXT", "glSecondaryColor3uiEXT", "glSecondaryColor3bvEXT", "glSecondaryColor3svEXT", "glSecondaryColor3ivEXT", "glSecondaryColor3fvEXT", "glSecondaryColor3dvEXT", "glSecondaryColor3ubvEXT", "glSecondaryColor3usvEXT", "glSecondaryColor3uivEXT", "glSecondaryColorPointerEXT") || Checks.reportMissing("GL", "GL_EXT_secondary_color");
    }

    private static boolean check_EXT_semaphore(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_semaphore")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1793, 1794, 1837, 1838, 1839, 1840, 1841, 1842, 1843}, "glGetUnsignedBytevEXT", "glGetUnsignedBytei_vEXT", "glGenSemaphoresEXT", "glDeleteSemaphoresEXT", "glIsSemaphoreEXT", "glSemaphoreParameterui64vEXT", "glGetSemaphoreParameterui64vEXT", "glWaitSemaphoreEXT", "glSignalSemaphoreEXT") || Checks.reportMissing("GL", "GL_EXT_semaphore");
    }

    private static boolean check_EXT_semaphore_fd(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_semaphore_fd")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1844}, "glImportSemaphoreFdEXT") || Checks.reportMissing("GL", "GL_EXT_semaphore_fd");
    }

    private static boolean check_EXT_semaphore_win32(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_semaphore_win32")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1845, 1846}, "glImportSemaphoreWin32HandleEXT", "glImportSemaphoreWin32NameEXT") || Checks.reportMissing("GL", "GL_EXT_semaphore_win32");
    }

    private static boolean check_EXT_separate_shader_objects(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_separate_shader_objects")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1847, 1848, 1849}, "glUseShaderProgramEXT", "glActiveProgramEXT", "glCreateShaderProgramEXT") || Checks.reportMissing("GL", "GL_EXT_separate_shader_objects");
    }

    private static boolean check_EXT_shader_framebuffer_fetch_non_coherent(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_shader_framebuffer_fetch_non_coherent")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1850}, "glFramebufferFetchBarrierEXT") || Checks.reportMissing("GL", "GL_EXT_shader_framebuffer_fetch_non_coherent");
    }

    private static boolean check_EXT_shader_image_load_store(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_shader_image_load_store")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1851, 1852}, "glBindImageTextureEXT", "glMemoryBarrierEXT") || Checks.reportMissing("GL", "GL_EXT_shader_image_load_store");
    }

    private static boolean check_EXT_stencil_clear_tag(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_stencil_clear_tag")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1853}, "glStencilClearTagEXT") || Checks.reportMissing("GL", "GL_EXT_stencil_clear_tag");
    }

    private static boolean check_EXT_stencil_two_side(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_stencil_two_side")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1854}, "glActiveStencilFaceEXT") || Checks.reportMissing("GL", "GL_EXT_stencil_two_side");
    }

    private static boolean check_EXT_texture_array(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_texture_array")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1755}, "glFramebufferTextureLayerEXT") || Checks.reportMissing("GL", "GL_EXT_texture_array");
    }

    private static boolean check_EXT_texture_buffer_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_texture_buffer_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1855}, "glTexBufferEXT") || Checks.reportMissing("GL", "GL_EXT_texture_buffer_object");
    }

    private static boolean check_EXT_texture_integer(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_texture_integer")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1856, 1857, 1858, 1859, 1860, 1861}, "glClearColorIiEXT", "glClearColorIuiEXT", "glTexParameterIivEXT", "glTexParameterIuivEXT", "glGetTexParameterIivEXT", "glGetTexParameterIuivEXT") || Checks.reportMissing("GL", "GL_EXT_texture_integer");
    }

    private static boolean check_EXT_texture_storage(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_texture_storage")) {
            return false;
        }
        int flag0 = GLCapabilities.hasDSA(ext) ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1862, 1863, 1864, flag0 + 1375, flag0 + 1376, flag0 + 1377}, "glTexStorage1DEXT", "glTexStorage2DEXT", "glTexStorage3DEXT", "glTextureStorage1DEXT", "glTextureStorage2DEXT", "glTextureStorage3DEXT") || Checks.reportMissing("GL", "GL_EXT_texture_storage");
    }

    private static boolean check_EXT_timer_query(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_timer_query")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1865, 1866}, "glGetQueryObjecti64vEXT", "glGetQueryObjectui64vEXT") || Checks.reportMissing("GL", "GL_EXT_timer_query");
    }

    private static boolean check_EXT_transform_feedback(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_transform_feedback")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1867, 1868, 1869, 1870, 1871, 1872, 1873, 1595, 1596}, "glBindBufferRangeEXT", "glBindBufferOffsetEXT", "glBindBufferBaseEXT", "glBeginTransformFeedbackEXT", "glEndTransformFeedbackEXT", "glTransformFeedbackVaryingsEXT", "glGetTransformFeedbackVaryingEXT", "glGetIntegerIndexedvEXT", "glGetBooleanIndexedvEXT") || Checks.reportMissing("GL", "GL_EXT_transform_feedback");
    }

    private static boolean check_EXT_vertex_attrib_64bit(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_vertex_attrib_64bit")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1874, 1875, 1876, 1877, 1878, 1879, 1880, 1881, 1882, 1883, flag0 + 1384}, "glVertexAttribL1dEXT", "glVertexAttribL2dEXT", "glVertexAttribL3dEXT", "glVertexAttribL4dEXT", "glVertexAttribL1dvEXT", "glVertexAttribL2dvEXT", "glVertexAttribL3dvEXT", "glVertexAttribL4dvEXT", "glVertexAttribLPointerEXT", "glGetVertexAttribLdvEXT", "glVertexArrayVertexAttribLOffsetEXT") || Checks.reportMissing("GL", "GL_EXT_vertex_attrib_64bit");
    }

    private static boolean check_EXT_win32_keyed_mutex(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_win32_keyed_mutex")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1884, 1885}, "glAcquireKeyedMutexWin32EXT", "glReleaseKeyedMutexWin32EXT") || Checks.reportMissing("GL", "GL_EXT_win32_keyed_mutex");
    }

    private static boolean check_EXT_window_rectangles(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_window_rectangles")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1886}, "glWindowRectanglesEXT") || Checks.reportMissing("GL", "GL_EXT_window_rectangles");
    }

    private static boolean check_EXT_x11_sync_object(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_EXT_x11_sync_object")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1887}, "glImportSyncEXT") || Checks.reportMissing("GL", "GL_EXT_x11_sync_object");
    }

    private static boolean check_GREMEDY_frame_terminator(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_GREMEDY_frame_terminator")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1888}, "glFrameTerminatorGREMEDY") || Checks.reportMissing("GL", "GL_GREMEDY_frame_terminator");
    }

    private static boolean check_GREMEDY_string_marker(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_GREMEDY_string_marker")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1889}, "glStringMarkerGREMEDY") || Checks.reportMissing("GL", "GL_GREMEDY_string_marker");
    }

    private static boolean check_INTEL_framebuffer_CMAA(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_INTEL_framebuffer_CMAA")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1890}, "glApplyFramebufferAttachmentCMAAINTEL") || Checks.reportMissing("GL", "GL_INTEL_framebuffer_CMAA");
    }

    private static boolean check_INTEL_map_texture(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_INTEL_map_texture")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1891, 1892, 1893}, "glSyncTextureINTEL", "glUnmapTexture2DINTEL", "glMapTexture2DINTEL") || Checks.reportMissing("GL", "GL_INTEL_map_texture");
    }

    private static boolean check_INTEL_performance_query(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_INTEL_performance_query")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1894, 1895, 1896, 1897, 1898, 1899, 1900, 1901, 1902, 1903}, "glBeginPerfQueryINTEL", "glCreatePerfQueryINTEL", "glDeletePerfQueryINTEL", "glEndPerfQueryINTEL", "glGetFirstPerfQueryIdINTEL", "glGetNextPerfQueryIdINTEL", "glGetPerfCounterInfoINTEL", "glGetPerfQueryDataINTEL", "glGetPerfQueryIdByNameINTEL", "glGetPerfQueryInfoINTEL") || Checks.reportMissing("GL", "GL_INTEL_performance_query");
    }

    private static boolean check_KHR_blend_equation_advanced(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_KHR_blend_equation_advanced")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1904}, "glBlendBarrierKHR") || Checks.reportMissing("GL", "GL_KHR_blend_equation_advanced");
    }

    private static boolean check_KHR_debug(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_KHR_debug")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{875, 876, 877, 878, 879, 880, 881, 882, 883, 884}, "glDebugMessageControl", "glDebugMessageInsert", "glDebugMessageCallback", "glGetDebugMessageLog", "glPushDebugGroup", "glPopDebugGroup", "glObjectLabel", "glGetObjectLabel", "glObjectPtrLabel", "glGetObjectPtrLabel") || Checks.reportMissing("GL", "GL_KHR_debug");
    }

    private static boolean check_KHR_parallel_shader_compile(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_KHR_parallel_shader_compile")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1905}, "glMaxShaderCompilerThreadsKHR") || Checks.reportMissing("GL", "GL_KHR_parallel_shader_compile");
    }

    private static boolean check_KHR_robustness(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_KHR_robustness")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1024, 1033, 1040, 1042, 1043}, "glGetGraphicsResetStatus", "glReadnPixels", "glGetnUniformfv", "glGetnUniformiv", "glGetnUniformuiv") || Checks.reportMissing("GL", "GL_KHR_robustness");
    }

    private static boolean check_MESA_framebuffer_flip_y(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_MESA_framebuffer_flip_y")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1906, 1907}, "glFramebufferParameteriMESA", "glGetFramebufferParameterivMESA") || Checks.reportMissing("GL", "GL_MESA_framebuffer_flip_y");
    }

    private static boolean check_NV_alpha_to_coverage_dither_control(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_alpha_to_coverage_dither_control")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1908}, "glAlphaToCoverageDitherControlNV") || Checks.reportMissing("GL", "GL_NV_alpha_to_coverage_dither_control");
    }

    private static boolean check_NV_bindless_multi_draw_indirect(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_bindless_multi_draw_indirect")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1909, 1910}, "glMultiDrawArraysIndirectBindlessNV", "glMultiDrawElementsIndirectBindlessNV") || Checks.reportMissing("GL", "GL_NV_bindless_multi_draw_indirect");
    }

    private static boolean check_NV_bindless_multi_draw_indirect_count(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_bindless_multi_draw_indirect_count")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1911, 1912}, "glMultiDrawArraysIndirectBindlessCountNV", "glMultiDrawElementsIndirectBindlessCountNV") || Checks.reportMissing("GL", "GL_NV_bindless_multi_draw_indirect_count");
    }

    private static boolean check_NV_bindless_texture(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_bindless_texture")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1913, 1914, 1915, 1916, 1917, 1918, 1919, 1920, 1921, 1922, 1923, 1924, 1925}, "glGetTextureHandleNV", "glGetTextureSamplerHandleNV", "glMakeTextureHandleResidentNV", "glMakeTextureHandleNonResidentNV", "glGetImageHandleNV", "glMakeImageHandleResidentNV", "glMakeImageHandleNonResidentNV", "glUniformHandleui64NV", "glUniformHandleui64vNV", "glProgramUniformHandleui64NV", "glProgramUniformHandleui64vNV", "glIsTextureHandleResidentNV", "glIsImageHandleResidentNV") || Checks.reportMissing("GL", "GL_NV_bindless_texture");
    }

    private static boolean check_NV_blend_equation_advanced(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_blend_equation_advanced")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1926, 1927}, "glBlendParameteriNV", "glBlendBarrierNV") || Checks.reportMissing("GL", "GL_NV_blend_equation_advanced");
    }

    private static boolean check_NV_clip_space_w_scaling(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_clip_space_w_scaling")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1928}, "glViewportPositionWScaleNV") || Checks.reportMissing("GL", "GL_NV_clip_space_w_scaling");
    }

    private static boolean check_NV_command_list(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_command_list")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1929, 1930, 1931, 1932, 1933, 1934, 1935, 1936, 1937, 1938, 1939, 1940, 1941, 1942, 1943, 1944, 1945}, "glCreateStatesNV", "glDeleteStatesNV", "glIsStateNV", "glStateCaptureNV", "glGetCommandHeaderNV", "glGetStageIndexNV", "glDrawCommandsNV", "glDrawCommandsAddressNV", "glDrawCommandsStatesNV", "glDrawCommandsStatesAddressNV", "glCreateCommandListsNV", "glDeleteCommandListsNV", "glIsCommandListNV", "glListDrawCommandsStatesClientNV", "glCommandListSegmentsNV", "glCompileCommandListNV", "glCallCommandListNV") || Checks.reportMissing("GL", "GL_NV_command_list");
    }

    private static boolean check_NV_conditional_render(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_conditional_render")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1946, 1947}, "glBeginConditionalRenderNV", "glEndConditionalRenderNV") || Checks.reportMissing("GL", "GL_NV_conditional_render");
    }

    private static boolean check_NV_conservative_raster(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_conservative_raster")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1948}, "glSubpixelPrecisionBiasNV") || Checks.reportMissing("GL", "GL_NV_conservative_raster");
    }

    private static boolean check_NV_conservative_raster_dilate(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_conservative_raster_dilate")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1949}, "glConservativeRasterParameterfNV") || Checks.reportMissing("GL", "GL_NV_conservative_raster_dilate");
    }

    private static boolean check_NV_conservative_raster_pre_snap_triangles(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_conservative_raster_pre_snap_triangles")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1950}, "glConservativeRasterParameteriNV") || Checks.reportMissing("GL", "GL_NV_conservative_raster_pre_snap_triangles");
    }

    private static boolean check_NV_copy_image(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_copy_image")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1951}, "glCopyImageSubDataNV") || Checks.reportMissing("GL", "GL_NV_copy_image");
    }

    private static boolean check_NV_depth_buffer_float(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_depth_buffer_float")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1952, 1953, 1954}, "glDepthRangedNV", "glClearDepthdNV", "glDepthBoundsdNV") || Checks.reportMissing("GL", "GL_NV_depth_buffer_float");
    }

    private static boolean check_NV_draw_texture(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_draw_texture")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1955}, "glDrawTextureNV") || Checks.reportMissing("GL", "GL_NV_draw_texture");
    }

    private static boolean check_NV_draw_vulkan_image(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_draw_vulkan_image")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1956, 1957, 1958, 1959, 1960}, "glDrawVkImageNV", "glGetVkProcAddrNV", "glWaitVkSemaphoreNV", "glSignalVkSemaphoreNV", "glSignalVkFenceNV") || Checks.reportMissing("GL", "GL_NV_draw_vulkan_image");
    }

    private static boolean check_NV_explicit_multisample(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_explicit_multisample")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1961, 1962, 1963}, "glGetMultisamplefvNV", "glSampleMaskIndexedNV", "glTexRenderbufferNV") || Checks.reportMissing("GL", "GL_NV_explicit_multisample");
    }

    private static boolean check_NV_fence(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_fence")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1964, 1965, 1966, 1967, 1968, 1969, 1970}, "glDeleteFencesNV", "glGenFencesNV", "glIsFenceNV", "glTestFenceNV", "glGetFenceivNV", "glFinishFenceNV", "glSetFenceNV") || Checks.reportMissing("GL", "GL_NV_fence");
    }

    private static boolean check_NV_fragment_coverage_to_color(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_fragment_coverage_to_color")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1971}, "glFragmentCoverageColorNV") || Checks.reportMissing("GL", "GL_NV_fragment_coverage_to_color");
    }

    private static boolean check_NV_framebuffer_mixed_samples(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_framebuffer_mixed_samples")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1819, 1972, 1973, 1974}, "glRasterSamplesEXT", "glCoverageModulationTableNV", "glGetCoverageModulationTableNV", "glCoverageModulationNV") || Checks.reportMissing("GL", "GL_NV_framebuffer_mixed_samples");
    }

    private static boolean check_NV_framebuffer_multisample_coverage(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_framebuffer_multisample_coverage")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1975}, "glRenderbufferStorageMultisampleCoverageNV") || Checks.reportMissing("GL", "GL_NV_framebuffer_multisample_coverage");
    }

    private static boolean check_NV_gpu_multicast(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_gpu_multicast")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{1976, 1977, 1978, 1979, 1980, 1981, 1982, 1983, 1984, 1985, 1986, 1987}, "glRenderGpuMaskNV", "glMulticastBufferSubDataNV", "glMulticastCopyBufferSubDataNV", "glMulticastCopyImageSubDataNV", "glMulticastBlitFramebufferNV", "glMulticastFramebufferSampleLocationsfvNV", "glMulticastBarrierNV", "glMulticastWaitSyncNV", "glMulticastGetQueryObjectivNV", "glMulticastGetQueryObjectuivNV", "glMulticastGetQueryObjecti64vNV", "glMulticastGetQueryObjectui64vNV") || Checks.reportMissing("GL", "GL_NV_gpu_multicast");
    }

    private static boolean check_NV_gpu_shader5(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_gpu_shader5")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_direct_state_access") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1058, 1059, 1060, 1061, 1062, 1063, 1064, 1065, 1066, 1067, 1068, 1069, 1070, 1071, 1072, 1073, 1074, 1075, flag0 + 1076, flag0 + 1077, flag0 + 1078, flag0 + 1079, flag0 + 1080, flag0 + 1081, flag0 + 1082, flag0 + 1083, flag0 + 1084, flag0 + 1085, flag0 + 1086, flag0 + 1087, flag0 + 1088, flag0 + 1089, flag0 + 1090, flag0 + 1091}, "glUniform1i64NV", "glUniform2i64NV", "glUniform3i64NV", "glUniform4i64NV", "glUniform1i64vNV", "glUniform2i64vNV", "glUniform3i64vNV", "glUniform4i64vNV", "glUniform1ui64NV", "glUniform2ui64NV", "glUniform3ui64NV", "glUniform4ui64NV", "glUniform1ui64vNV", "glUniform2ui64vNV", "glUniform3ui64vNV", "glUniform4ui64vNV", "glGetUniformi64vNV", "glGetUniformui64vNV", "glProgramUniform1i64NV", "glProgramUniform2i64NV", "glProgramUniform3i64NV", "glProgramUniform4i64NV", "glProgramUniform1i64vNV", "glProgramUniform2i64vNV", "glProgramUniform3i64vNV", "glProgramUniform4i64vNV", "glProgramUniform1ui64NV", "glProgramUniform2ui64NV", "glProgramUniform3ui64NV", "glProgramUniform4ui64NV", "glProgramUniform1ui64vNV", "glProgramUniform2ui64vNV", "glProgramUniform3ui64vNV", "glProgramUniform4ui64vNV") || Checks.reportMissing("GL", "GL_NV_gpu_shader5");
    }

    private static boolean check_NV_half_float(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_half_float")) {
            return false;
        }
        int flag0 = ext.contains("GL_EXT_fog_coord") ? 0 : Integer.MIN_VALUE;
        int flag2 = ext.contains("GL_EXT_secondary_color") ? 0 : Integer.MIN_VALUE;
        int flag4 = ext.contains("GL_EXT_vertex_weighting") ? 0 : Integer.MIN_VALUE;
        int flag6 = ext.contains("GL_NV_vertex_program") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{1988, 1989, 1990, 1991, 1992, 1993, 1994, 1995, 1996, 1997, 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, flag0 + 2016, flag0 + 2017, flag2 + 2018, flag2 + 2019, flag4 + 2020, flag4 + 2021, flag6 + 2022, flag6 + 2023, flag6 + 2024, flag6 + 2025, flag6 + 2026, flag6 + 2027, flag6 + 2028, flag6 + 2029, flag6 + 2030, flag6 + 2031, flag6 + 2032, flag6 + 2033}, "glVertex2hNV", "glVertex2hvNV", "glVertex3hNV", "glVertex3hvNV", "glVertex4hNV", "glVertex4hvNV", "glNormal3hNV", "glNormal3hvNV", "glColor3hNV", "glColor3hvNV", "glColor4hNV", "glColor4hvNV", "glTexCoord1hNV", "glTexCoord1hvNV", "glTexCoord2hNV", "glTexCoord2hvNV", "glTexCoord3hNV", "glTexCoord3hvNV", "glTexCoord4hNV", "glTexCoord4hvNV", "glMultiTexCoord1hNV", "glMultiTexCoord1hvNV", "glMultiTexCoord2hNV", "glMultiTexCoord2hvNV", "glMultiTexCoord3hNV", "glMultiTexCoord3hvNV", "glMultiTexCoord4hNV", "glMultiTexCoord4hvNV", "glFogCoordhNV", "glFogCoordhvNV", "glSecondaryColor3hNV", "glSecondaryColor3hvNV", "glVertexWeighthNV", "glVertexWeighthvNV", "glVertexAttrib1hNV", "glVertexAttrib1hvNV", "glVertexAttrib2hNV", "glVertexAttrib2hvNV", "glVertexAttrib3hNV", "glVertexAttrib3hvNV", "glVertexAttrib4hNV", "glVertexAttrib4hvNV", "glVertexAttribs1hvNV", "glVertexAttribs2hvNV", "glVertexAttribs3hvNV", "glVertexAttribs4hvNV") || Checks.reportMissing("GL", "GL_NV_half_float");
    }

    private static boolean check_NV_internalformat_sample_query(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_internalformat_sample_query")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2034}, "glGetInternalformatSampleivNV") || Checks.reportMissing("GL", "GL_NV_internalformat_sample_query");
    }

    private static boolean check_NV_memory_attachment(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_memory_attachment")) {
            return false;
        }
        int flag0 = GLCapabilities.hasDSA(ext) ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{2035, 2036, 2037, 2038, flag0 + 2039, flag0 + 2040}, "glGetMemoryObjectDetachedResourcesuivNV", "glResetMemoryObjectParameterNV", "glTexAttachMemoryNV", "glBufferAttachMemoryNV", "glTextureAttachMemoryNV", "glNamedBufferAttachMemoryNV") || Checks.reportMissing("GL", "GL_NV_memory_attachment");
    }

    private static boolean check_NV_memory_object_sparse(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_memory_object_sparse")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2041, 2042, 2043, 2044}, "glBufferPageCommitmentMemNV", "glNamedBufferPageCommitmentMemNV", "glTexPageCommitmentMemNV", "glTexturePageCommitmentMemNV") || Checks.reportMissing("GL", "GL_NV_memory_object_sparse");
    }

    private static boolean check_NV_mesh_shader(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_mesh_shader")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2045, 2046, 2047, 2048}, "glDrawMeshTasksNV", "glDrawMeshTasksIndirectNV", "glMultiDrawMeshTasksIndirectNV", "glMultiDrawMeshTasksIndirectCountNV") || Checks.reportMissing("GL", "GL_NV_mesh_shader");
    }

    private static boolean check_NV_path_rendering(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_path_rendering")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2049, 2050, 2051, 2052, 2053, 2054, 2055, 2058, 2060, 2061, 2062, 2063, 2064, 2065, 2066, 2067, 2068, 2069, 2070, 2071, 2072, 2073, 2074, 2075, 2076, 2080, 2081, 2082, 2083, 2090, 2091, 2092, 2093, 2094, 2095, 2096, 2097, 2102, 2103, 2104, 2105}, "glPathCommandsNV", "glPathCoordsNV", "glPathSubCommandsNV", "glPathSubCoordsNV", "glPathStringNV", "glPathGlyphsNV", "glPathGlyphRangeNV", "glCopyPathNV", "glInterpolatePathsNV", "glTransformPathNV", "glPathParameterivNV", "glPathParameteriNV", "glPathParameterfvNV", "glPathParameterfNV", "glPathDashArrayNV", "glGenPathsNV", "glDeletePathsNV", "glIsPathNV", "glPathStencilFuncNV", "glPathStencilDepthOffsetNV", "glStencilFillPathNV", "glStencilStrokePathNV", "glStencilFillPathInstancedNV", "glStencilStrokePathInstancedNV", "glPathCoverDepthFuncNV", "glCoverFillPathNV", "glCoverStrokePathNV", "glCoverFillPathInstancedNV", "glCoverStrokePathInstancedNV", "glGetPathParameterivNV", "glGetPathParameterfvNV", "glGetPathCommandsNV", "glGetPathCoordsNV", "glGetPathDashArrayNV", "glGetPathMetricsNV", "glGetPathMetricRangeNV", "glGetPathSpacingNV", "glIsPointInFillPathNV", "glIsPointInStrokePathNV", "glGetPathLengthNV", "glPointAlongPathNV") || Checks.reportMissing("GL", "GL_NV_path_rendering");
    }

    private static boolean check_NV_pixel_data_range(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_pixel_data_range")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2113, 2114}, "glPixelDataRangeNV", "glFlushPixelDataRangeNV") || Checks.reportMissing("GL", "GL_NV_pixel_data_range");
    }

    private static boolean check_NV_point_sprite(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_point_sprite")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2115, 2116}, "glPointParameteriNV", "glPointParameterivNV") || Checks.reportMissing("GL", "GL_NV_point_sprite");
    }

    private static boolean check_NV_primitive_restart(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_primitive_restart")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2117, 2118}, "glPrimitiveRestartNV", "glPrimitiveRestartIndexNV") || Checks.reportMissing("GL", "GL_NV_primitive_restart");
    }

    private static boolean check_NV_query_resource(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_query_resource")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2119}, "glQueryResourceNV") || Checks.reportMissing("GL", "GL_NV_query_resource");
    }

    private static boolean check_NV_query_resource_tag(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_query_resource_tag")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2120, 2121, 2122}, "glGenQueryResourceTagNV", "glDeleteQueryResourceTagNV", "glQueryResourceTagNV") || Checks.reportMissing("GL", "GL_NV_query_resource_tag");
    }

    private static boolean check_NV_sample_locations(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_sample_locations")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2123, 2124, 2125}, "glFramebufferSampleLocationsfvNV", "glNamedFramebufferSampleLocationsfvNV", "glResolveDepthValuesNV") || Checks.reportMissing("GL", "GL_NV_sample_locations");
    }

    private static boolean check_NV_scissor_exclusive(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_scissor_exclusive")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2126, 2127}, "glScissorExclusiveArrayvNV", "glScissorExclusiveNV") || Checks.reportMissing("GL", "GL_NV_scissor_exclusive");
    }

    private static boolean check_NV_shader_buffer_load(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_shader_buffer_load")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2128, 2129, 2130, 2131, 2132, 2133, 2134, 2135, 2136, 2137, 2138, 1075, 2139, 2140}, "glMakeBufferResidentNV", "glMakeBufferNonResidentNV", "glIsBufferResidentNV", "glMakeNamedBufferResidentNV", "glMakeNamedBufferNonResidentNV", "glIsNamedBufferResidentNV", "glGetBufferParameterui64vNV", "glGetNamedBufferParameterui64vNV", "glGetIntegerui64vNV", "glUniformui64NV", "glUniformui64vNV", "glGetUniformui64vNV", "glProgramUniformui64NV", "glProgramUniformui64vNV") || Checks.reportMissing("GL", "GL_NV_shader_buffer_load");
    }

    private static boolean check_NV_shading_rate_image(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_shading_rate_image")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2141, 2142, 2143, 2144, 2145, 2146, 2147}, "glBindShadingRateImageNV", "glShadingRateImagePaletteNV", "glGetShadingRateImagePaletteNV", "glShadingRateImageBarrierNV", "glShadingRateSampleOrderNV", "glShadingRateSampleOrderCustomNV", "glGetShadingRateSampleLocationivNV") || Checks.reportMissing("GL", "GL_NV_shading_rate_image");
    }

    private static boolean check_NV_texture_barrier(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_texture_barrier")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2148}, "glTextureBarrierNV") || Checks.reportMissing("GL", "GL_NV_texture_barrier");
    }

    private static boolean check_NV_texture_multisample(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_texture_multisample")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2149, 2150, 2151, 2152, 2153, 2154}, "glTexImage2DMultisampleCoverageNV", "glTexImage3DMultisampleCoverageNV", "glTextureImage2DMultisampleNV", "glTextureImage3DMultisampleNV", "glTextureImage2DMultisampleCoverageNV", "glTextureImage3DMultisampleCoverageNV") || Checks.reportMissing("GL", "GL_NV_texture_multisample");
    }

    private static boolean check_NV_timeline_semaphore(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_timeline_semaphore")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2155, 2156, 2157}, "glCreateSemaphoresNV", "glSemaphoreParameterivNV", "glGetSemaphoreParameterivNV") || Checks.reportMissing("GL", "GL_NV_timeline_semaphore");
    }

    private static boolean check_NV_transform_feedback(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_transform_feedback")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2158, 2159, 2160, 2161, 2162, 2163, 2164, 2165, 2166, 2167, 2168, 2169}, "glBeginTransformFeedbackNV", "glEndTransformFeedbackNV", "glTransformFeedbackAttribsNV", "glBindBufferRangeNV", "glBindBufferOffsetNV", "glBindBufferBaseNV", "glTransformFeedbackVaryingsNV", "glActiveVaryingNV", "glGetVaryingLocationNV", "glGetActiveVaryingNV", "glGetTransformFeedbackVaryingNV", "glTransformFeedbackStreamAttribsNV") || Checks.reportMissing("GL", "GL_NV_transform_feedback");
    }

    private static boolean check_NV_transform_feedback2(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_transform_feedback2")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2170, 2171, 2172, 2173, 2174, 2175, 2176}, "glBindTransformFeedbackNV", "glDeleteTransformFeedbacksNV", "glGenTransformFeedbacksNV", "glIsTransformFeedbackNV", "glPauseTransformFeedbackNV", "glResumeTransformFeedbackNV", "glDrawTransformFeedbackNV") || Checks.reportMissing("GL", "GL_NV_transform_feedback2");
    }

    private static boolean check_NV_vertex_array_range(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_vertex_array_range")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2177, 2178}, "glVertexArrayRangeNV", "glFlushVertexArrayRangeNV") || Checks.reportMissing("GL", "GL_NV_vertex_array_range");
    }

    private static boolean check_NV_vertex_attrib_integer_64bit(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_vertex_attrib_integer_64bit")) {
            return false;
        }
        int flag0 = ext.contains("GL_NV_vertex_buffer_unified_memory") ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{2179, 2180, 2181, 2182, 2183, 2184, 2185, 2186, 2187, 2188, 2189, 2190, 2191, 2192, 2193, 2194, 2195, 2196, flag0 + 2197}, "glVertexAttribL1i64NV", "glVertexAttribL2i64NV", "glVertexAttribL3i64NV", "glVertexAttribL4i64NV", "glVertexAttribL1i64vNV", "glVertexAttribL2i64vNV", "glVertexAttribL3i64vNV", "glVertexAttribL4i64vNV", "glVertexAttribL1ui64NV", "glVertexAttribL2ui64NV", "glVertexAttribL3ui64NV", "glVertexAttribL4ui64NV", "glVertexAttribL1ui64vNV", "glVertexAttribL2ui64vNV", "glVertexAttribL3ui64vNV", "glVertexAttribL4ui64vNV", "glGetVertexAttribLi64vNV", "glGetVertexAttribLui64vNV", "glVertexAttribLFormatNV") || Checks.reportMissing("GL", "GL_NV_vertex_attrib_integer_64bit");
    }

    private static boolean check_NV_vertex_buffer_unified_memory(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_vertex_buffer_unified_memory")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2198, 2199, 2200, 2201, 2202, 2203, 2204, 2205, 2206, 2207, 2208, 2209}, "glBufferAddressRangeNV", "glVertexFormatNV", "glNormalFormatNV", "glColorFormatNV", "glIndexFormatNV", "glTexCoordFormatNV", "glEdgeFlagFormatNV", "glSecondaryColorFormatNV", "glFogCoordFormatNV", "glVertexAttribFormatNV", "glVertexAttribIFormatNV", "glGetIntegerui64i_vNV") || Checks.reportMissing("GL", "GL_NV_vertex_buffer_unified_memory");
    }

    private static boolean check_NV_viewport_swizzle(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NV_viewport_swizzle")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2210}, "glViewportSwizzleNV") || Checks.reportMissing("GL", "GL_NV_viewport_swizzle");
    }

    private static boolean check_NVX_conditional_render(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NVX_conditional_render")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2211, 2212}, "glBeginConditionalRenderNVX", "glEndConditionalRenderNVX") || Checks.reportMissing("GL", "GL_NVX_conditional_render");
    }

    private static boolean check_NVX_gpu_multicast2(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NVX_gpu_multicast2")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2213, 2214, 2215, 2216, 2217, 2218}, "glAsyncCopyImageSubDataNVX", "glAsyncCopyBufferSubDataNVX", "glUploadGpuMaskNVX", "glMulticastViewportArrayvNVX", "glMulticastScissorArrayvNVX", "glMulticastViewportPositionWScaleNVX") || Checks.reportMissing("GL", "GL_NVX_gpu_multicast2");
    }

    private static boolean check_NVX_progress_fence(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_NVX_progress_fence")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{2219, 2220, 2221, 2222}, "glCreateProgressFenceNVX", "glSignalSemaphoreui64NVX", "glWaitSemaphoreui64NVX", "glClientWaitSemaphoreui64NVX") || Checks.reportMissing("GL", "GL_NVX_progress_fence");
    }

    private static boolean check_OVR_multiview(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("GL_OVR_multiview")) {
            return false;
        }
        int flag0 = GLCapabilities.hasDSA(ext) ? 0 : Integer.MIN_VALUE;
        return Checks.checkFunctions(provider, caps, new int[]{2223, flag0 + 2224}, "glFramebufferTextureMultiviewOVR", "glNamedFramebufferTextureMultiviewOVR") || Checks.reportMissing("GL", "GL_OVR_multiview");
    }

    private static boolean hasDSA(Set<String> ext) {
        return ext.contains("GL45") || ext.contains("GL_ARB_direct_state_access") || ext.contains("GL_EXT_direct_state_access");
    }

    private static boolean ARB_framebuffer_object(Set<String> ext) {
        return ext.contains("OpenGL30") || ext.contains("GL_ARB_framebuffer_object");
    }

    private static boolean ARB_map_buffer_range(Set<String> ext) {
        return ext.contains("OpenGL30") || ext.contains("GL_ARB_map_buffer_range");
    }

    private static boolean ARB_vertex_array_object(Set<String> ext) {
        return ext.contains("OpenGL30") || ext.contains("GL_ARB_vertex_array_object");
    }

    private static boolean ARB_copy_buffer(Set<String> ext) {
        return ext.contains("OpenGL31") || ext.contains("GL_ARB_copy_buffer");
    }

    private static boolean ARB_texture_buffer_object(Set<String> ext) {
        return ext.contains("OpenGL31") || ext.contains("GL_ARB_texture_buffer_object");
    }

    private static boolean ARB_uniform_buffer_object(Set<String> ext) {
        return ext.contains("OpenGL31") || ext.contains("GL_ARB_uniform_buffer_object");
    }

    private static boolean ARB_instanced_arrays(Set<String> ext) {
        return ext.contains("OpenGL33") || ext.contains("GL_ARB_instanced_arrays");
    }

    private static boolean ARB_sampler_objects(Set<String> ext) {
        return ext.contains("OpenGL33") || ext.contains("GL_ARB_sampler_objects");
    }

    private static boolean ARB_transform_feedback2(Set<String> ext) {
        return ext.contains("OpenGL40") || ext.contains("GL_ARB_transform_feedback2");
    }

    private static boolean ARB_vertex_attrib_64bit(Set<String> ext) {
        return ext.contains("OpenGL41") || ext.contains("GL_ARB_vertex_attrib_64bit");
    }

    private static boolean ARB_separate_shader_objects(Set<String> ext) {
        return ext.contains("OpenGL41") || ext.contains("GL_ARB_separate_shader_objects");
    }

    private static boolean ARB_texture_storage(Set<String> ext) {
        return ext.contains("OpenGL42") || ext.contains("GL_ARB_texture_storage");
    }

    private static boolean ARB_texture_storage_multisample(Set<String> ext) {
        return ext.contains("OpenGL43") || ext.contains("GL_ARB_texture_storage_multisample");
    }

    private static boolean ARB_vertex_attrib_binding(Set<String> ext) {
        return ext.contains("OpenGL43") || ext.contains("GL_ARB_vertex_attrib_binding");
    }

    private static boolean ARB_invalidate_subdata(Set<String> ext) {
        return ext.contains("OpenGL43") || ext.contains("GL_ARB_invalidate_subdata");
    }

    private static boolean ARB_texture_buffer_range(Set<String> ext) {
        return ext.contains("OpenGL43") || ext.contains("GL_ARB_texture_buffer_range");
    }

    private static boolean ARB_clear_buffer_object(Set<String> ext) {
        return ext.contains("OpenGL43") || ext.contains("GL_ARB_clear_buffer_object");
    }

    private static boolean ARB_framebuffer_no_attachments(Set<String> ext) {
        return ext.contains("OpenGL43") || ext.contains("GL_ARB_framebuffer_no_attachments");
    }

    private static boolean ARB_buffer_storage(Set<String> ext) {
        return ext.contains("OpenGL44") || ext.contains("GL_ARB_buffer_storage");
    }

    private static boolean ARB_clear_texture(Set<String> ext) {
        return ext.contains("OpenGL44") || ext.contains("GL_ARB_clear_texture");
    }

    private static boolean ARB_multi_bind(Set<String> ext) {
        return ext.contains("OpenGL44") || ext.contains("GL_ARB_multi_bind");
    }

    private static boolean ARB_query_buffer_object(Set<String> ext) {
        return ext.contains("OpenGL44") || ext.contains("GL_ARB_query_buffer_object");
    }
}

