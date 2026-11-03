// SPDX-FileCopyrightText: BepInEx contributors
// SPDX-License-Identifier: LGPL-2.1-only
//
// Adapted from BepInEx v6.0.0-pre.2, commit
// 53625800b86f6c68751445248260edf0b27a71c2, file
// Runtimes/Unity/BepInEx.Unity.IL2CPP/Il2CppInteropManager.AsmToCecilConverter.cs.
// The upstream type is private. This source-preserved adapter exposes the same serialization bridge
// to CrewCore's pinned command-line orchestration without changing the pristine upstream checkout.

using Mono.Cecil;
using CecilAssemblyDefinition = Mono.Cecil.AssemblyDefinition;
using ResolverAssemblyDefinition = AsmResolver.DotNet.AssemblyDefinition;

namespace CrewCore.V3.InteropPipeline.ThirdParty.BepInEx;

internal sealed class AsmToCecilConverter : IAssemblyResolver
{
    private readonly Dictionary<string, ResolverAssemblyDefinition> source;
    private readonly Dictionary<string, CecilAssemblyDefinition> convertedByName = new(StringComparer.Ordinal);
    private readonly Dictionary<ResolverAssemblyDefinition, CecilAssemblyDefinition> converted = new();

    public AsmToCecilConverter(List<ResolverAssemblyDefinition> assemblies)
    {
        source = assemblies.ToDictionary(
            assembly => assembly.Name?.ToString() ?? throw new InvalidOperationException("Cpp2IL emitted an assembly without a name."),
            assembly => assembly,
            StringComparer.Ordinal);
    }

    public void Dispose() { }

    public CecilAssemblyDefinition Resolve(AssemblyNameReference name) =>
        Resolve(name, new ReaderParameters { AssemblyResolver = this });

    public CecilAssemblyDefinition Resolve(AssemblyNameReference name, ReaderParameters parameters)
    {
        if (convertedByName.TryGetValue(name.Name, out var value)) return value;
        if (!source.TryGetValue(name.Name, out var sourceAssembly)) throw new AssemblyResolutionException(name);
        return Convert(sourceAssembly, parameters);
    }

    public List<CecilAssemblyDefinition> ConvertAll() => source.Values.Select(Convert).ToList();

    private CecilAssemblyDefinition Convert(ResolverAssemblyDefinition sourceAssembly) =>
        Convert(sourceAssembly, new ReaderParameters { AssemblyResolver = this });

    private CecilAssemblyDefinition Convert(ResolverAssemblyDefinition sourceAssembly, ReaderParameters parameters)
    {
        if (converted.TryGetValue(sourceAssembly, out var value)) return value;
        var stream = new MemoryStream();
        sourceAssembly.WriteManifest(stream);
        stream.Position = 0;
        value = CecilAssemblyDefinition.ReadAssembly(stream, parameters);
        convertedByName.Add(value.Name.Name, value);
        converted.Add(sourceAssembly, value);
        return value;
    }
}
