import os
import glob
import re

def fix_all(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    lines = content.split('\n')
    out_lines = []

    for line in lines:
        # Fix PackageName
        if line.startswith('package tg.univlome.saas.marketing.automation.domain.services.Impl;'):
            line = 'package tg.univlome.saas.marketing.automation.domain.services.impl;'
            
        # Fix UnusedImports in WorkflowExecutionController
        if file_path.endswith('WorkflowExecutionController.java'):
            if line.startswith('import org.springframework.web.bind.annotation.DeleteMapping;'):
                continue
            if line.startswith('import org.springframework.web.bind.annotation.PutMapping;'):
                continue

        # Fix LineLength
        if len(line) > 140:
            # specifically for @Operation descriptions
            if '@Operation' in line and 'description = "' in line:
                line = line.replace('", description = "', '",\n            description = "')
            if '@ApiResponse' in line and 'description = "' in line:
                line = line.replace('", description = "', '",\n                    description = "')
            # For WorkflowExecutionServiceImpl:68 (List<WorkflowExecutionResponse> getExecutionsByWorkflow(UUID workflowTrackingId); in interface, or similar)
            # wait, it says WorkflowExecutionServiceImpl.java:68 "La ligne excède 140 caractères (trouvé 156)"
            # That might be a method declaration or exception thrown.
            # I can just break on common things
            if '-> new ResourceNotFoundException(' in line:
                line = line.replace('-> new ResourceNotFoundException(', '-> \n                    new ResourceNotFoundException(')

        out_lines.append(line)

    # Re-join with \n
    new_content = '\n'.join(out_lines)
    
    # Ensure exact one NewlineAtEndOfFile
    new_content = new_content.rstrip() + '\n'

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)

java_files = glob.glob('src/main/java/tg/univlome/saas/marketing/automation/**/*.java', recursive=True)
test_files = glob.glob('src/test/java/tg/univlome/saas/marketing/automation/**/*.java', recursive=True)
for jf in java_files + test_files:
    fix_all(jf)
    print(f"Fixed {jf}")
