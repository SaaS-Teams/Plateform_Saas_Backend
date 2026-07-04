import os
import re

files_to_fix = [
    "src/main/java/tg/univlome/saas/marketing/campagne/domain/models/Campagne.java",
    "src/main/java/tg/univlome/saas/marketing/campagne/domain/services/CampagneService.java",
    "src/main/java/tg/univlome/saas/marketing/campagne/domain/services/impl/CampagneServiceImpl.java",
    "src/main/java/tg/univlome/saas/marketing/campagne/repositories/CampagneRepository.java",
    "src/main/java/tg/univlome/saas/marketing/campagne/application/controllers/CampagneController.java",
    "src/main/java/tg/univlome/saas/marketing/campagne/application/dtos/response/CampagneResponse.java",
]

for file_path in files_to_fix:
    with open(file_path, "r") as f:
        content = f.read()

    # ensure newline at end
    if not content.endswith("\n"):
        content += "\n"

    # extract imports
    lines = content.split('\n')
    non_imports = []
    imports = []
    for line in lines:
        if line.startswith("import "):
            imports.append(line)
        else:
            non_imports.append(line)

    imports.sort()

    # reconstruct file: package -> imports -> rest
    # find where to insert imports
    out_lines = []
    inserted_imports = False
    for line in non_imports:
        if line.startswith("package "):
            out_lines.append(line)
            out_lines.append("")
        elif not inserted_imports and line.strip() == "":
            pass # skip empty lines before class def
        elif not inserted_imports and (line.startswith("public ") or line.startswith("@") or line.startswith("class ") or line.startswith("public record") or line.startswith("public interface")):
            out_lines.extend(imports)
            out_lines.append("")
            out_lines.append(line)
            inserted_imports = True
        else:
            out_lines.append(line)

    with open(file_path, "w") as f:
        f.write('\n'.join(out_lines))

print("Fixed imports")
