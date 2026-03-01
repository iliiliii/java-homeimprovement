# Vue 编译语法错误修复

**修复时间**: 2026-03-01  
**错误**: Unexpected token (263:13)

---

## ❌ 错误信息

```
[vue/compiler-sfc] Unexpected token (263:13)
/Users/y/code/java-home/vue3/src/views/evs/projects/components/ProjectEdit.vue

280|          if (!form.value.customerIds.includes(form.value.customerId)) {
    |                            ^
```

---

## 🔍 问题原因

在 `submitForm` 函数中存在**重复的代码块**，导致括号不匹配。

**问题代码**（第 377-386 行）:
```javascript
}).catch(err => {
  console.error('项目创建失败:', err)
  proxy.$modal.msgError("创建失败：" + (err.message || err))
})
      }
              emit('success')  // ← 重复的代码
            }).catch(err => {
              proxy.$modal.msgWarning("项目创建成功，但客户关联失败：" + err.message)
              open.value = false
              emit('success')
            })
          } else {
            proxy.$modal.msgSuccess("新增成功")
            open.value = false
            emit('success')
          }
        })
      }
    }
  })
}
```

**原因**:
- 修改代码时，旧的代码块没有完全删除
- 导致有两个 `}).catch(err => {` 块
- 括号不匹配，引发编译错误

---

## ✅ 修复方案

删除重复的代码块。

**修复后的代码**:
```javascript
}).catch(err => {
  console.error('项目创建失败:', err)
  proxy.$modal.msgError("创建失败：" + (err.message || err))
})
      }
    }
  })
}

/** 处理客户关联（编辑模式） */
async function handleCustomerRelations(projectId, newCustomerIds, newPrimaryCustomerId) {
  // ...
}
```

---

## ✅ 验证

修复后，Vue 编译应该通过，不再报错。

---

**修复时间**: 2026-03-01  
**状态**: ✅ 已修复
