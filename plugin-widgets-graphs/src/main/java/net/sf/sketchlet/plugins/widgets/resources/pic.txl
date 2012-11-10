% Copyright 2009 Mykhaylo Sorochan
%
% Redistribution and use in source and binary forms, with or without
% modification, are permitted provided that the following conditions are met:
%
%    Redistributions of source code must retain the above copyright notice,
%    this list of conditions and the following disclaimer.
%    Redistributions in binary form must reproduce the above copyright notice,
%    this list of conditions and the following disclaimer in the documentation
%    and/or other materials provided with the distribution.
%
%    THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
%    INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
%    AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
%    AUTHORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
%    OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
%    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
%    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
%    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
%    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
%    POSSIBILITY OF SUCH DAMAGE.
 
 
include "sequence.Grm"
 
function main
    replace [program]
        P [program]
    by
        P [doTransform]
end function
 
 
function doTransform
    replace [program]
        P [program]
    % import command line args
    import TXLargs [repeat stringlit]
    deconstruct * TXLargs
        "-sqdfile" FileName [stringlit] MoreOptions [repeat stringlit]
    % parse UML class diagram file
    construct SqDFF [sequence_diagram]
        _ [read FileName]
    % preparations
    deconstruct SqDFF
        SysParams [repeat param_assignment] Obj4Id [repeat obj_decl] _ [repeat seq_d_stmt]
    construct SqD [sequence_diagram]
        SqDFF [backward2ForwardMessagesA] [backward2ForwardMessagesS]
              [emptyMessageTransformF] [emptyMessageTransformR]
              [convertIDs each Obj4Id]
    deconstruct SqD
        _ [repeat param_assignment] Objs [repeat obj_decl] Stmts [repeat seq_d_stmt]
    export ObjDecls [repeat obj_decl]
        Objs
 
    % collect comment objects declaration
    construct CommentDecls [repeat comment_decl]
        _ [collectCommentDecls Objs]
    export CommentDecls
 
    by
        P [addSystemConfig each SysParams]
          [addObjDecl each Objs]
          [addSqDStmts each Stmts]
%          [addFinalStep]
          [completeLifelines Stmts each Objs]
end function
 
function addSystemConfig SysParam [param_assignment]
    replace * [repeat param_assignment]
        P [repeat param_assignment]
    by
        P [. SysParam]
end function
 
rule backward2ForwardMessagesA
    replace [object_message]
        I1 [id] '<- I2 [id] ML [opt message_label]
    by
        I2 '-> I1  ML
end rule
 
rule backward2ForwardMessagesS
    replace [object_message]
        I1 [id] '<= I2 [id] ML [opt message_label]
    by
        I2 '=> I1  ML
end rule
 
rule emptyMessageTransformF
    replace [forward_message]
        I1 [id] Op [forward_message_op] I2 [id]
    by
        I1 Op I2  ', ""
end rule
 
rule emptyMessageTransformR
    replace [return_message]
        I1 [id] Op [return_message_op] I2 [id]
    by
        I1 Op I2  ', ""
end rule
 
 
function convertIDs ObjDecl [obj_decl]
    deconstruct * [id] ObjDecl
        OldId [id]
    construct NewId [id]
        _ [+ 'OID] [!]
    replace [sequence_diagram]
        P [sequence_diagram]
    by
        P [replaceIDs OldId NewId]
end function
 
rule replaceIDs OldId [id] NewId [id]
    replace [id]
        OldId
    by
        NewId
end rule
 
 
function collectCommentDecls Objs [repeat obj_decl]
    replace [repeat comment_decl]
        _ [repeat comment_decl]
    construct C [repeat comment_decl]
        _ [^ Objs]
    by
        C
end function
 
 
function addObjDecl O [obj_decl]
    replace * [repeat pic_funcall]
        PFs [repeat pic_funcall]
    construct PF [repeat pic_funcall]
        _ [convertObjDecl O] [convertActorDecl O] [convertPObjectDecl O] [convertDummyDecl O]
    by
        PFs [. PF]
end function
 
function convertObjDecl O [obj_decl]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    deconstruct O
        Id [id] ', Label [stringlit] C [opt obj_constraint]
    construct PF [repeat pic_funcall]
        'object '( Id ', Label ') ';
    by
        PF [addObjConstraint C]
end function
 
function convertActorDecl O [obj_decl]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    deconstruct O
        Id [id] ': _ [actor_type] ', Label [stringlit] C [opt obj_constraint]
    construct PF [repeat pic_funcall]
        'actor '( Id ', Label ') ';
    by
        PF [addObjConstraint C]
end function
 
function convertPObjectDecl O [obj_decl]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    deconstruct O
        Id [id] ': _ [placeholder_type] ', _ [stringlit] _ [opt obj_constraint]
    construct PF [repeat pic_funcall]
        'pobject '( Id ') ';
    by
        PF
end function
 
function convertDummyDecl O [obj_decl]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    deconstruct O
        Id [id] ': _ [dummy_type]
    construct PF [repeat pic_funcall]
        'pobject '( Id ') ';
    by
        PF
end function
 
 
function addObjConstraint C [opt obj_constraint]
    deconstruct C
        '^ Label [stringlit]
    replace [repeat pic_funcall]
        PFs [repeat pic_funcall]
    construct CLabel [stringlit]
        _ [+ "{"] [+ Label] [+ "}"]
 
    construct PF [pic_funcall]
        'oconstraint( CLabel ') ';
    by
        PFs [. PF]
end function
 
 
function addSqDStmts S [seq_d_stmt]
    replace * [repeat pic_funcall]
        PFs [repeat pic_funcall]
    construct PF [repeat pic_funcall]
        _ [convertForwardMsgA S] [convertForwardMsgS S]
          [convertCreateMsgA S] [convertCreateMsgS S]
          [convertDestroyMsgA S] [convertDestroyMsgS S]
          [convertReturnMsgA S] [convertReturnMsgS S]
          [convertDestroyCommand S]
          [convertStepCommand S]
          [convertConstraintAbove S] [convertConstraintBelow S]
          [convertCommentCommand S] [convertCommentCommandFirst S]
          [convertMessageBlockOpen S] [convertMessageBlockClose S] [convertFrame S]
    by
        PFs [. PF]
end function
 
 
function convertForwardMsgA S [seq_d_stmt]
    deconstruct S
        Id1 [id] '-> Id2 [id] ', Label [stringlit]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'message_a( Id1 ', Id2 ', Label ');
    by
        PF
end function
 
function convertForwardMsgS S [seq_d_stmt]
    deconstruct S
        Id1 [id] '=> Id2 [id] ', Label [stringlit]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'message_s( Id1 ', Id2 ', Label ');
    by
        PF
end function
 
 
function convertCreateMsgA S [seq_d_stmt]
    deconstruct S
        Id1 [id] '*-> Id2 [id]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct Label [stringlit]
        _ [addPObjectMessage Id2]
    construct PF [repeat pic_funcall]
        'cmessage_a( Id1 ', Id2 ', Label ');
    by
        PF [addPObjectConstraint Id2]
end function
 
function convertCreateMsgS S [seq_d_stmt]
    deconstruct S
        Id1 [id] '*=> Id2 [id]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct Label [stringlit]
        _ [addPObjectMessage Id2]
    construct PF [repeat pic_funcall]
        'cmessage_s( Id1 ', Id2 ', Label ');
    by
        PF [addPObjectConstraint Id2]
end function
 
function addPObjectMessage PObjId [id]
    import ObjDecls [repeat obj_decl]
    deconstruct * [obj_decl] ObjDecls
        PObjId ': _ [placeholder_type] ', Label [stringlit] _ [opt obj_constraint]
    replace [stringlit]
        _ [stringlit]
    by
        Label
end function
 
function addPObjectConstraint PobjId [id]
    import ObjDecls [repeat obj_decl]
    deconstruct * [obj_decl] ObjDecls
        PObjId ': _ [placeholder_type] ', _ [stringlit] C [opt obj_constraint]
    deconstruct C
        '^ Label [stringlit]
    replace [repeat pic_funcall]
        PFs [repeat pic_funcall]
    construct CLabel [stringlit]
        _ [+ "{"] [+ Label] [+ "}"]
    construct PF [repeat pic_funcall]
        'oconstraint( PObjId ', CLabel ');
    by
        PFs [. PF]
end function
 
 
function convertDestroyMsgA S [seq_d_stmt]
    deconstruct S
        Id1 [id] '~-> Id2 [id]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'dmessage_a( Id1 ', Id2 ');
    by
        PF
end function
 
function convertDestroyMsgS S [seq_d_stmt]
    deconstruct S
        Id1 [id] '~=> Id2 [id]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'dmessage_s( Id1 ', Id2 ');
    by
        PF
end function
 
 
function convertReturnMsgA S [seq_d_stmt]
    deconstruct S
        Id1 [id] '<-- Id2 [id] ', Label [stringlit]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'rmessage_a( Id2 ', Id1 ', Label ');
    by
        PF
end function
 
function convertReturnMsgS S [seq_d_stmt]
    deconstruct S
        Id1 [id] '<== Id2 [id] ', Label [stringlit]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'rmessage_s( Id2 ', Id1 ', Label ');
    by
        PF
end function
 
 
function convertDestroyCommand S [seq_d_stmt]
    deconstruct S
        '~ Id [id]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'delete( Id ');
    by
        PF
end function
 
 
function convertStepCommand S [seq_d_stmt]
    deconstruct S
        '|
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'step();
    by
        PF
end function
 
 
function convertConstraintAbove S [seq_d_stmt]
    deconstruct S
        Id [id] '^+ Str [stringlit]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct StrC [stringlit]
        _ [+ "{"] [+ Str] [+ "}"]
    construct PF [pic_funcall]
        'lconstraint( Id ', StrC ');
    by
        PF
end function
 
function convertConstraintBelow S [seq_d_stmt]
    deconstruct S
        Id [id] '^- Str [stringlit]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct StrC [stringlit]
        _ [+ "{"] [+ Str] [+ "}"]
    construct PF [pic_funcall]
        'lconstraint_below( Id ', StrC ');
    by
        PF
end function
 
 
function convertCommentCommandFirst S [seq_d_stmt]
    deconstruct S
        Id [id] '-- CommentId [id]
    import CommentDecls [repeat comment_decl]
    deconstruct * [comment_decl] CommentDecls
        CommentId ': _ [comment_type] CG [opt comment_geometry] ', Strings [repeat stringlit+]
    construct NewCommentDecls [repeat comment_decl]
        _ [removeCommentDecl CommentId each CommentDecls]
    export CommentDecls
        NewCommentDecls
 
    construct CGP [repeat comment_geometry_position]
        _ [getCGP CG]
    construct CGS [repeat comment_geometry_size]
        _ [getCGS CG]
 
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [pic_funcall]
        'comment( Id ', CommentId ', CGP ', CGS  Strings ');
    by
        PF
end function
 
function getCGP CG [opt comment_geometry]
    deconstruct CG
        ', CGP [repeat comment_geometry_position] ', _ [repeat comment_geometry_size]
    replace [repeat comment_geometry_position]
        _ [repeat comment_geometry_position]
    by
        CGP
end function
 
function getCGS CG [opt comment_geometry]
    deconstruct CG
        ', _ [repeat comment_geometry_position] ', CGS [repeat comment_geometry_size]
    replace [repeat comment_geometry_size]
        _ [repeat comment_geometry_size]
    by
        CGS
end function
 
 
function removeCommentDecl CId [id] CD [comment_decl]
    replace [repeat comment_decl]
        CDs [repeat comment_decl]
    deconstruct not * [comment_decl] CD
        CId ': _ [comment_type] _ [opt comment_geometry] ', _ [repeat stringlit+]
    by
        CDs [. CD]
end function
 
function convertCommentCommand S [seq_d_stmt]
    deconstruct S
        Id [id] '-- CommentId [id]
    import CommentDecls [repeat comment_decl]
    deconstruct not * [comment_decl] CommentDecls
        CommentId ': _ [comment_type] _ [opt comment_geometry] ', Strings [repeat stringlit+]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct PF [ pic_funcall]
        'connect_to_comment( Id ', CommentId ');
    by
        PF
end function
 
 
function convertMessageBlockOpen S [seq_d_stmt]
    deconstruct S
        Id [id] '[
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct BeginPF [repeat pic_funcall]
        'active( Id ');
    by
        BeginPF
end function
 
function convertMessageBlockClose S [seq_d_stmt]
    deconstruct S
        '] Id [id]
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct EndPF [pic_funcall]
        'inactive( Id ');
    by
        EndPF
end function
 
 
function convertFrame S [seq_d_stmt]
    deconstruct S
        IdFrom [id] ', IdTo [id] ', L [stringlit] '{ Stmts [repeat seq_d_stmt] '}
    replace [repeat pic_funcall]
        _ [repeat pic_funcall]
    construct FrameId [id]
        _ [+ "Frame"] [!]
    construct BeginPF [repeat pic_funcall]
        'begin_frame( IdFrom ', FrameId ', L ');
    construct EndPF [pic_funcall]
        'end_frame( IdTo ', FrameId ');
    construct PFs [repeat pic_funcall]
        _ [addSqDStmts each Stmts]
    by
        BeginPF [. PFs] [. EndPF]
end function
 
 
function addFinalStep
    replace * [repeat pic_funcall]
        PFs [repeat pic_funcall]
    construct PF [pic_funcall]
        'step();
    by
        PFs [. PF]
end function
 
function completeLifelines Stmts [repeat seq_d_stmt] Obj [obj_decl]
    deconstruct * [id] Obj
        Id [id]
    deconstruct not * [destroy_command] Stmts
        '~ Id
    deconstruct not * [destroy_message] Stmts
        _ [id] _ [destroy_message_op] Id
    deconstruct not * [comment_decl] Obj
        _ [comment_decl]
    deconstruct not * [dummy_decl] Obj
        _ [dummy_decl]
 
    where not
        Obj [?isFirstPObject]
    where not
        Obj [?isLastNotCreatedPOBject Stmts]
 
    replace * [repeat pic_funcall]
        PFs [repeat pic_funcall]
    construct PF [pic_funcall]
        'complete( Id ');
    by
        PFs [. PF]
end function
 
function isFirstPObject
    replace [obj_decl]
        Obj [obj_decl]
    deconstruct Obj
        _ [placeholder_decl] _ [opt obj_constraint]
    import ObjDecls [repeat obj_decl]
    deconstruct ObjDecls
        Obj _ [repeat obj_decl]
    by
        Obj
end function
 
function isLastNotCreatedPOBject Stmts [repeat seq_d_stmt]
    replace [obj_decl]
        Obj [obj_decl]
    deconstruct Obj
        Id [id] ': _ [placeholder_type] ', _ [stringlit] _ [opt obj_constraint]
 
    import ObjDecls [repeat obj_decl]
    deconstruct * ObjDecls
        LastObj [obj_decl]
 
    where all
        Obj [?isLast LastObj] [?wasNotCreated Stmts]
    by
        Obj
end function
 
function isLast LastObj [obj_decl]
    replace [obj_decl]
        Obj [obj_decl]
    deconstruct Obj
        LastObj
    by
        LastObj
end function
 
 
function wasNotCreated Stmts [repeat seq_d_stmt]
    replace [obj_decl]
        O [obj_decl]
    deconstruct O
        Id [id] ': _ [placeholder_type] ', _ [stringlit] _ [opt obj_constraint]
    deconstruct not * [create_message] Stmts
        _ [id] _ [create_message_op] Id
    by
        O
end function

articles/uml-sequence-diagram-dsl-txl/pic.txl.txt · Last modified: 2009/08/28 09:18 by macroexpand
