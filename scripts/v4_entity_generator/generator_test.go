package main

import (
	"testing"
)

func TestGenerate(t *testing.T) {
	if err := NewGenerator().Generate("../../src/main/idls/thrift/shared.thrift", "../../.gen", "com.uber.cadence.entities"); err != nil {
		t.Fatalf("failed to generate: %v", err)
	}
}
